package de.adorsys.ledgers.deposit.api.service.impl;

import de.adorsys.ledgers.deposit.api.domain.*;
import de.adorsys.ledgers.deposit.api.exception.DepositAccountNotFoundException;
import de.adorsys.ledgers.deposit.api.exception.DepositAccountUncheckedException;
import de.adorsys.ledgers.deposit.api.exception.TransactionNotFoundException;
import de.adorsys.ledgers.deposit.api.service.DepositAccountConfigService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.deposit.api.service.mappers.DepositAccountMapper;
import de.adorsys.ledgers.deposit.api.service.mappers.TransactionDetailsMapper;
import de.adorsys.ledgers.deposit.db.domain.DepositAccount;
import de.adorsys.ledgers.deposit.db.repository.DepositAccountRepository;
import de.adorsys.ledgers.postings.api.domain.*;
import de.adorsys.ledgers.postings.api.exception.BaseLineException;
import de.adorsys.ledgers.postings.api.exception.LedgerAccountNotFoundException;
import de.adorsys.ledgers.postings.api.exception.LedgerNotFoundException;
import de.adorsys.ledgers.postings.api.exception.PostingNotFoundException;
import de.adorsys.ledgers.postings.api.service.AccountStmtService;
import de.adorsys.ledgers.postings.api.service.LedgerService;
import de.adorsys.ledgers.postings.api.service.PostingService;
import de.adorsys.ledgers.util.Ids;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepositAccountServiceImpl extends AbstractServiceImpl implements DepositAccountService {
    private static final Logger logger = LoggerFactory.getLogger(DepositAccountServiceImpl.class);

    private final DepositAccountRepository depositAccountRepository;
    private final DepositAccountMapper depositAccountMapper;
    private final AccountStmtService accountStmtService;
    private final PostingService postingService;
    private final TransactionDetailsMapper transactionDetailsMapper;

    public DepositAccountServiceImpl(DepositAccountConfigService depositAccountConfigService, LedgerService ledgerService, DepositAccountRepository depositAccountRepository, DepositAccountMapper depositAccountMapper, AccountStmtService accountStmtService, PostingService postingService, TransactionDetailsMapper transactionDetailsMapper) {
        super(depositAccountConfigService, ledgerService);
        this.depositAccountRepository = depositAccountRepository;
        this.depositAccountMapper = depositAccountMapper;
        this.accountStmtService = accountStmtService;
        this.postingService = postingService;
        this.transactionDetailsMapper = transactionDetailsMapper;
    }

    @Override
    public DepositAccountBO createDepositAccount(DepositAccountBO depositAccountBO) throws DepositAccountNotFoundException {
        DepositAccount depositAccount = depositAccountMapper.toDepositAccount(depositAccountBO);

        LedgerBO ledgerBO = loadLedger();

        String depositParentAccountNbr = depositAccountConfigService.getDepositParentAccount();
        LedgerAccountBO depositParentAccount = new LedgerAccountBO();
        depositParentAccount.setLedger(ledgerBO);
        depositParentAccount.setName(depositParentAccountNbr);

        // Business logic
        LedgerAccountBO ledgerAccount = new LedgerAccountBO();
        ledgerAccount.setParent(depositParentAccount);
        ledgerAccount.setName(depositAccount.getIban());


        try {
            ledgerService.newLedgerAccount(ledgerAccount);
        } catch (LedgerAccountNotFoundException | LedgerNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new DepositAccountNotFoundException(e.getMessage(), e);
        }

        DepositAccount da = createDepositAccountObj(depositAccount);

        DepositAccount saved = depositAccountRepository.save(da);
        return depositAccountMapper.toDepositAccountBO(saved);
    }

    private DepositAccount createDepositAccountObj(DepositAccount depositAccount) {
        DepositAccount da = new DepositAccount();
        da.setId(Ids.id());
        da.setAccountStatus(depositAccount.getAccountStatus());
        da.setAccountType(depositAccount.getAccountType());
        da.setCurrency(depositAccount.getCurrency());
        da.setDetails(depositAccount.getDetails());
        da.setIban(depositAccount.getIban());
        da.setLinkedAccounts(depositAccount.getLinkedAccounts());
        da.setMsisdn(depositAccount.getMsisdn());
        da.setName(depositAccount.getName());
        da.setProduct(depositAccount.getProduct());
        da.setUsageType(depositAccount.getUsageType());

        return depositAccountRepository.save(da);
    }

    @Override
    public DepositAccountBO getDepositAccountById(String accountId) throws DepositAccountNotFoundException {
        return depositAccountRepository.findById(accountId)
                       .map(depositAccountMapper::toDepositAccountBO)
                       .orElseThrow(() -> new DepositAccountNotFoundException(accountId));
    }

    @Override
    public DepositAccountBO getDepositAccountByIBAN(String iban) throws DepositAccountNotFoundException {
        return depositAccountRepository.findByIban(iban)
                       .map(depositAccountMapper::toDepositAccountBO)
                       .orElseThrow(() -> new DepositAccountNotFoundException(iban));
    }

    @Override
    public List<BalanceBO> getBalances(String iban) throws LedgerAccountNotFoundException {
        LedgerBO ledger = loadLedger();
        LedgerAccountBO ledgerAccountBO = newLedgerAccountBOObj(ledger, iban);
        List<BalanceBO> result = new ArrayList<>();
        try {
            AccountStmtBO stmt = accountStmtService.readStmt(ledgerAccountBO, LocalDateTime.now());
            BalanceBO balanceBO = new BalanceBO();
            BalanceSideBO balanceSide = stmt.getAccount().getBalanceSide();
            AmountBO amount = new AmountBO();
            balanceBO.setAmount(amount);
            if (BalanceSideBO.Cr.equals(balanceSide)) {
                amount.setAmount(stmt.creditBalance());
            } else {
                amount.setAmount(stmt.creditBalance());
            }
            balanceBO.setBalanceType(BalanceTypeBO.AVAILABLE);
            PostingTraceBO youngestPst = stmt.getYoungestPst();
            balanceBO.setReferenceDate(stmt.getPstTime().toLocalDate());
            if (youngestPst != null) {
                balanceBO.setLastChangeDateTime(youngestPst.getSrcPstTime());
                balanceBO.setLastCommittedTransaction(youngestPst.getSrcPstId());
            } else {
                balanceBO.setLastChangeDateTime(stmt.getPstTime());
            }
            result.add(balanceBO);
        } catch (LedgerNotFoundException | BaseLineException e) {
            throw new IllegalStateException(e);
        }
        return result;
    }

    @Override
    public List<DepositAccountBO> getDepositAccountsByIBAN(List<String> ibans) {
        logger.info("Retrieving deposit accounts by list of IBANs");

        List<DepositAccount> accounts = depositAccountRepository.findByIbanIn(ibans);
        logger.info("{} IBANs were found", accounts.size());

        return depositAccountMapper.toDepositAccountListBO(accounts);
    }

    @Override
    public List<TransactionDetailsBO> getTransactionsByDates(String accountId, LocalDateTime dateFrom, LocalDateTime dateTo) throws DepositAccountNotFoundException {
        DepositAccountBO account = getDepositAccountById(accountId);
        LedgerBO ledgerBO = loadLedger();
        LedgerAccountBO ledgerAccountBO;
        try {
            ledgerAccountBO = ledgerService.findLedgerAccount(ledgerBO, account.getIban());
            return postingService.findPostingsByDates(ledgerAccountBO, dateFrom, dateTo)
                           .stream()
                           .map(transactionDetailsMapper::toTransaction)
                           .collect(Collectors.toList());
        } catch (LedgerNotFoundException | LedgerAccountNotFoundException e) {
            throw new DepositAccountUncheckedException(e.getMessage(), e);
        }
    }

    @Override
    public TransactionDetailsBO getTransactionById(String accountId, String transactionId) throws TransactionNotFoundException {
        try {
            DepositAccountBO account = getDepositAccountById(accountId);
            LedgerBO ledgerBO = loadLedger();
            LedgerAccountBO ledgerAccountBO = ledgerService.findLedgerAccount(ledgerBO, account.getIban());
            PostingLineBO line = postingService.findPostingLineById(ledgerAccountBO, transactionId);
            return transactionDetailsMapper.toTransaction(line);
        } catch (DepositAccountNotFoundException | LedgerNotFoundException | LedgerAccountNotFoundException | PostingNotFoundException e) {
            throw new TransactionNotFoundException(e.getMessage());
        }
    }

    private LedgerAccountBO newLedgerAccountBOObj(LedgerBO ledger, String iban) {
        LedgerAccountBO ledgerAccountBO = new LedgerAccountBO();
        ledgerAccountBO.setName(iban);
        ledgerAccountBO.setLedger(ledger);
        return ledgerAccountBO;
    }
}
