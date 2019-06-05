package de.adorsys.ledgers.deposit.api.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.ledgers.deposit.api.domain.*;
import de.adorsys.ledgers.deposit.api.exception.DepositAccountAlreadyExistsException;
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
import de.adorsys.ledgers.postings.api.exception.*;
import de.adorsys.ledgers.postings.api.service.AccountStmtService;
import de.adorsys.ledgers.postings.api.service.LedgerService;
import de.adorsys.ledgers.postings.api.service.PostingService;
import de.adorsys.ledgers.util.Ids;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DepositAccountServiceImpl extends AbstractServiceImpl implements DepositAccountService {
    private static final Logger logger = LoggerFactory.getLogger(DepositAccountServiceImpl.class);
    private static final String msgIbanNF = "Accounts with iban %s not found";

    private final DepositAccountRepository depositAccountRepository;
    private final DepositAccountMapper depositAccountMapper;
    private final AccountStmtService accountStmtService;
    private final PostingService postingService;
    private final TransactionDetailsMapper transactionDetailsMapper;
    private final ObjectMapper objectMapper;

    public DepositAccountServiceImpl(DepositAccountConfigService depositAccountConfigService,
                                     LedgerService ledgerService, DepositAccountRepository depositAccountRepository,
                                     DepositAccountMapper depositAccountMapper, AccountStmtService accountStmtService,
                                     PostingService postingService, TransactionDetailsMapper transactionDetailsMapper,
                                     ObjectMapper objectMapper) {
        super(depositAccountConfigService, ledgerService);
        this.depositAccountRepository = depositAccountRepository;
        this.depositAccountMapper = depositAccountMapper;
        this.accountStmtService = accountStmtService;
        this.postingService = postingService;
        this.transactionDetailsMapper = transactionDetailsMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public DepositAccountBO createDepositAccount(DepositAccountBO depositAccountBO, String userName) throws DepositAccountNotFoundException {
        checkDepositAccountAlreadyExist(depositAccountBO);
        DepositAccount da = createDepositAccountObj(depositAccountBO, userName);
        DepositAccount saved = depositAccountRepository.save(da);
        return depositAccountMapper.toDepositAccountBO(saved);
    }

    @Override
    public DepositAccountBO createDepositAccountForBranch(DepositAccountBO depositAccountBO, String userName, String branch) throws DepositAccountNotFoundException {
        checkDepositAccountAlreadyExist(depositAccountBO);
        DepositAccount da = createDepositAccountObj(depositAccountBO,userName);
        da.setBranch(branch);
        DepositAccount saved = depositAccountRepository.save(da);
        return depositAccountMapper.toDepositAccountBO(saved);
    }

    @Override
    public DepositAccountDetailsBO getDepositAccountByIban(String iban, LocalDateTime refTime, boolean withBalances) throws DepositAccountNotFoundException {
        List<DepositAccountBO> accounts = getDepositAccountsByIban(Collections.singletonList(iban));

        if (accounts.isEmpty()) {
            throw new DepositAccountNotFoundException(String.format(msgIbanNF, iban));
        }
        DepositAccountBO account = accounts.iterator().next();
        return new DepositAccountDetailsBO(account, getBalancesList(account, withBalances, refTime));
    }

    @Override
    public List<DepositAccountDetailsBO> getDepositAccountsByIban(List<String> ibans, LocalDateTime refTime, boolean withBalances) throws DepositAccountNotFoundException {
        List<DepositAccountDetailsBO> result = new ArrayList<>();
        for (String iban : ibans) {
            result.add(getDepositAccountByIban(iban, refTime, withBalances));
        }
        return result;
    }

    @Override
    public DepositAccountDetailsBO getDepositAccountById(String accountId, LocalDateTime refTime, boolean withBalances) throws DepositAccountNotFoundException {
        DepositAccountBO depositAccountBO = getDepositAccountById(accountId);
        return new DepositAccountDetailsBO(depositAccountBO, getBalancesList(depositAccountBO, withBalances, refTime));
    }

    @Override
    public TransactionDetailsBO getTransactionById(String accountId, String transactionId) throws TransactionNotFoundException {
        try {
            DepositAccountBO account = getDepositAccountById(accountId);
            LedgerBO ledgerBO = loadLedger();
            LedgerAccountBO ledgerAccountBO = ledgerService.findLedgerAccount(ledgerBO, account.getIban());
            PostingLineBO line = postingService.findPostingLineById(ledgerAccountBO, transactionId);
            return transactionDetailsMapper.toTransactionSigned(line);
        } catch (DepositAccountNotFoundException | LedgerNotFoundException | LedgerAccountNotFoundException | PostingNotFoundException e) {
            throw new TransactionNotFoundException(e.getMessage());
        }
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
                           .map(transactionDetailsMapper::toTransactionSigned)
                           .collect(Collectors.toList());
        } catch (LedgerNotFoundException | LedgerAccountNotFoundException e) {
            throw new DepositAccountUncheckedException(e.getMessage(), e);
        }
    }

    @Override
    public boolean confirmationOfFunds(FundsConfirmationRequestBO requestBO) throws DepositAccountNotFoundException {
        try {
            DepositAccountDetailsBO account = getDepositAccountByIban(requestBO.getPsuAccount().getIban(), LocalDateTime.now(), true);
            return account.getBalances().stream()
                           .filter(b -> b.getBalanceType() == BalanceTypeBO.INTERIM_AVAILABLE)
                           .findFirst()
                           .map(b -> isSufficientAmountAvailable(requestBO, b))
                           .orElse(Boolean.FALSE);
        } catch (DepositAccountNotFoundException e) {
            logger.error(e.getMessage());
            throw new DepositAccountNotFoundException(e.getMessage(), e);
        }
    }

    @Override
    public String readIbanById(String id) {
        return depositAccountRepository.findById(id).map(DepositAccount::getIban).orElse(null);
    }

    @Override
    public List<DepositAccountBO> findByAccountNumberPrefix(String accountNumberPrefix) {
        List<DepositAccount> accounts = depositAccountRepository.findByIbanStartingWith(accountNumberPrefix);
        return depositAccountMapper.toDepositAccountListBO(accounts);
    }

    @Override
    public List<DepositAccountDetailsBO> findByBranch(String branch) {
        List<DepositAccount> accounts = depositAccountRepository.findByBranch(branch);
        List<DepositAccountBO> accountsBO = depositAccountMapper.toDepositAccountListBO(accounts);
        List<DepositAccountDetailsBO> accountDetails = new ArrayList<>();
        for (DepositAccountBO accountBO : accountsBO) {
            accountDetails.add(new DepositAccountDetailsBO(accountBO, Collections.emptyList()));
        }
        return accountDetails;
    }

    @Override
    public void depositCash(String accountId, AmountBO amount, String recordUser) throws DepositAccountNotFoundException {
        if (amount.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new DepositAccountUncheckedException("Deposited amount must be greater than zero");
        }

        DepositAccount depositAccount = depositAccountRepository.findById(accountId)
                                                .orElseThrow(DepositAccountNotFoundException::new);
        AccountReferenceBO accountReference = depositAccountMapper.toAccountReferenceBO(depositAccount);
        if (!accountReference.getCurrency().equals(amount.getCurrency())) {
            throw new DepositAccountUncheckedException("Deposited amount and account currencies are different");
        }

        LedgerBO ledger = loadLedger();
        LocalDateTime postingDateTime = LocalDateTime.now();

        depositCash(accountReference, amount, recordUser, ledger, postingDateTime);
    }

    private void checkDepositAccountAlreadyExist(DepositAccountBO depositAccountBO) {
        Optional<DepositAccount> depositAccount = depositAccountRepository.findByIbanAndCurrency(depositAccountBO.getIban(), depositAccountBO.getCurrency().getCurrencyCode());
        if(depositAccount.isPresent()) {
            String message = String.format("Deposit account already exists. IBAN %s. Currency %s",
                                           depositAccountBO.getIban(), depositAccountBO.getCurrency().getCurrencyCode());
            logger.error(message);
            throw new DepositAccountAlreadyExistsException(message);
        }
    }

    private DepositAccount createDepositAccountObj(DepositAccountBO depositAccountBO, String userName) throws DepositAccountNotFoundException {
        DepositAccount depositAccount = depositAccountMapper.toDepositAccount(depositAccountBO);

        LedgerBO ledgerBO = loadLedger();
        String depositParentAccountNbr = depositAccountConfigService.getDepositParentAccount();
        LedgerAccountBO depositParentAccount = new LedgerAccountBO(depositParentAccountNbr, ledgerBO);

        LedgerAccountBO ledgerAccount = new LedgerAccountBO(depositAccount.getIban(), depositParentAccount);

        try {
            ledgerService.newLedgerAccount(ledgerAccount, userName);
        } catch (LedgerAccountNotFoundException | LedgerNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new DepositAccountNotFoundException(e.getMessage(), e);
        }
        return depositAccountMapper.createDepositAccountObj(depositAccount);
    }

    private List<BalanceBO> getBalancesList(DepositAccountBO d, boolean withBalances, LocalDateTime refTime) {
        return withBalances
                       ? getBalances(d.getIban(), d.getCurrency(), refTime)
                       : Collections.emptyList();
    }

    private DepositAccountBO getDepositAccountById(String accountId) throws DepositAccountNotFoundException {
        return depositAccountRepository.findById(accountId)
                       .map(depositAccountMapper::toDepositAccountBO)
                       .orElseThrow(() -> new DepositAccountNotFoundException(accountId));
    }

    private List<BalanceBO> getBalances(String iban, Currency currency, LocalDateTime refTime) {
        LedgerBO ledger = loadLedger();
        LedgerAccountBO ledgerAccountBO = newLedgerAccountBOObj(ledger, iban);
        List<BalanceBO> result = new ArrayList<>();
        try { //TODO @DMIEX to be refactored, mostly moved to mapper
            AccountStmtBO stmt = accountStmtService.readStmt(ledgerAccountBO, refTime);
            BalanceBO balanceBO = new BalanceBO();
            AmountBO amount = new AmountBO();
            amount.setCurrency(currency);
            balanceBO.setAmount(amount);
            amount.setAmount(stmt.creditBalance());
            balanceBO.setBalanceType(BalanceTypeBO.INTERIM_AVAILABLE);
            PostingTraceBO youngestPst = stmt.getYoungestPst();
            balanceBO.setReferenceDate(stmt.getPstTime().toLocalDate());
            if (youngestPst != null) {
                balanceBO.setLastChangeDateTime(youngestPst.getSrcPstTime());
                balanceBO.setLastCommittedTransaction(youngestPst.getSrcPstId());
            } else {
                balanceBO.setLastChangeDateTime(stmt.getPstTime());
            }
            result.add(balanceBO);
        } catch (LedgerNotFoundException | BaseLineException | LedgerAccountNotFoundException e) {
            logger.error(e.getMessage(), e);
            throw new DepositAccountUncheckedException(e.getMessage(), e);
        }
        return result;
    }

    private List<DepositAccountBO> getDepositAccountsByIban(List<String> ibans) {
        logger.info("Retrieving deposit accounts by list of IBANs");

        List<DepositAccount> accounts = depositAccountRepository.findByIbanIn(ibans);
        logger.info("{} IBANs were found", accounts.size());

        return depositAccountMapper.toDepositAccountListBO(accounts);
    }

    private boolean isSufficientAmountAvailable(FundsConfirmationRequestBO request, BalanceBO balance) {
        AmountBO balanceAmount = balance.getAmount();
        return Optional.ofNullable(request.getInstructedAmount())
                       .map(r -> balanceAmount.getAmount().compareTo(r.getAmount()) >= 0)
                       .orElse(false);
    }

    private LedgerAccountBO newLedgerAccountBOObj(LedgerBO ledger, String iban) {
        LedgerAccountBO ledgerAccountBO = new LedgerAccountBO();
        ledgerAccountBO.setName(iban);
        ledgerAccountBO.setLedger(ledger);
        return ledgerAccountBO;
    }

    private void depositCash(AccountReferenceBO accountReference, AmountBO amount, String recordUser, LedgerBO ledger, LocalDateTime postingDateTime) {
        PostingBO posting = new PostingBO();
        posting.setLedger(ledger);
        posting.setPstTime(postingDateTime);
        posting.setOprDetails("Cash Deposit");
        posting.setOprId(Ids.id());
        posting.setPstType(PostingTypeBO.BUSI_TX);
        posting.setRecordUser(recordUser);

        // debit line
        String cashAccountName = depositAccountConfigService.getCashAccount();
        LedgerAccountBO debitAccount;
        try {
            debitAccount = ledgerService.findLedgerAccount(ledger, cashAccountName);
        } catch (LedgerNotFoundException | LedgerAccountNotFoundException e) {
            throw new DepositAccountUncheckedException(e.getMessage(), e);
        }
        String debitLineId = Ids.id();
        String debitTransactionDetails = newTransactionDetails(amount, accountReference, postingDateTime, debitLineId);
        PostingLineBO debitLine = newPostingLine(debitLineId, debitAccount, amount.getAmount(), BigDecimal.ZERO, debitTransactionDetails);
        posting.getLines().add(debitLine);

        // credit line
        LedgerAccountBO creditAccount;
        try {
            creditAccount = ledgerService.findLedgerAccount(ledger, accountReference.getIban());
        } catch (LedgerNotFoundException | LedgerAccountNotFoundException e) {
            throw new DepositAccountUncheckedException(e.getMessage(), e);
        }
        String creditLineId = Ids.id();
        String creditTransactionDetails = newTransactionDetails(amount, accountReference, postingDateTime, creditLineId);
        PostingLineBO creditLine = newPostingLine(creditLineId, creditAccount, BigDecimal.ZERO, amount.getAmount(), creditTransactionDetails);
        posting.getLines().add(creditLine);

        try {
            postingService.newPosting(posting);
        } catch (PostingNotFoundException | LedgerNotFoundException | LedgerAccountNotFoundException | BaseLineException | DoubleEntryAccountingException e) {
            throw new DepositAccountUncheckedException(e.getMessage(), e);
        }
    }

    private PostingLineBO newPostingLine(String id, LedgerAccountBO account, BigDecimal debitAmount, BigDecimal creditAmount, String details) {
        PostingLineBO debitLine = new PostingLineBO();
        debitLine.setId(id);
        debitLine.setAccount(account);
        debitLine.setDebitAmount(debitAmount);
        debitLine.setCreditAmount(creditAmount);
        debitLine.setDetails(details);
        return debitLine;
    }

    private String newTransactionDetails(AmountBO amount, AccountReferenceBO creditor, LocalDateTime postingDateTime, String postingLineId) {
        TransactionDetailsBO transactionDetails = new TransactionDetailsBO();
        transactionDetails.setTransactionId(Ids.id());
        transactionDetails.setEndToEndId(postingLineId);
        transactionDetails.setBookingDate(postingDateTime.toLocalDate());
        transactionDetails.setValueDate(postingDateTime.toLocalDate());
        transactionDetails.setTransactionAmount(amount);
        transactionDetails.setCreditorAccount(creditor);
        try {
            return objectMapper.writeValueAsString(transactionDetails);
        } catch (JsonProcessingException e) {
            throw new DepositAccountUncheckedException(e.getMessage(), e);
        }
    }
}
