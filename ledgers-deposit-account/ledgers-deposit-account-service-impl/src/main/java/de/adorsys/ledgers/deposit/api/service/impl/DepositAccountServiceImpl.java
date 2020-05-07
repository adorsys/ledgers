package de.adorsys.ledgers.deposit.api.service.impl;

import de.adorsys.ledgers.deposit.api.domain.*;
import de.adorsys.ledgers.deposit.api.service.CurrencyExchangeRatesService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountConfigService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.deposit.api.service.mappers.DepositAccountMapper;
import de.adorsys.ledgers.deposit.api.service.mappers.TransactionDetailsMapper;
import de.adorsys.ledgers.deposit.db.domain.DepositAccount;
import de.adorsys.ledgers.deposit.db.repository.DepositAccountRepository;
import de.adorsys.ledgers.postings.api.domain.*;
import de.adorsys.ledgers.postings.api.service.AccountStmtService;
import de.adorsys.ledgers.postings.api.service.LedgerService;
import de.adorsys.ledgers.postings.api.service.PostingService;
import de.adorsys.ledgers.util.Ids;
import de.adorsys.ledgers.util.exception.DepositModuleException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.mapstruct.factory.Mappers;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static de.adorsys.ledgers.deposit.api.domain.BalanceTypeBO.CLOSING_BOOKED;
import static de.adorsys.ledgers.deposit.api.domain.BalanceTypeBO.INTERIM_AVAILABLE;
import static de.adorsys.ledgers.util.exception.DepositErrorCode.*;
import static java.lang.String.format;

@Slf4j
@Service
public class DepositAccountServiceImpl extends AbstractServiceImpl implements DepositAccountService {
    private static final String MSG_IBAN_NOT_FOUND = "Accounts with iban %s and currency %s not found";
    private static final String MSG_ACCOUNT_NOT_FOUND = "Account with id %s not found";
    private static final String DELETE_BRANCH_ERROR_MSG = "Something went wrong during deletion of branch: %s, msg: %s";
    private static final String BRANCH_SQL = "classpath:deleteBranch.sql";
    private static final String POSTING_SQL = "classpath:deletePostings.sql";
    private static final String DELETE_POSTINGS_ERROR_MSG = "Something went wrong during deletion of postings for iban: %s, msg: %s";

    @PersistenceContext
    private final EntityManager entityManager;
    private final ResourceLoader loader;
    private final DepositAccountRepository depositAccountRepository;
    private final DepositAccountMapper depositAccountMapper = Mappers.getMapper(DepositAccountMapper.class);
    private final AccountStmtService accountStmtService;
    private final PostingService postingService;
    private final TransactionDetailsMapper transactionDetailsMapper;
    private final CurrencyExchangeRatesService exchangeRatesService;

    public DepositAccountServiceImpl(DepositAccountConfigService depositAccountConfigService,
                                     LedgerService ledgerService, EntityManager entityManager, ResourceLoader loader, DepositAccountRepository depositAccountRepository,
                                     AccountStmtService accountStmtService,
                                     PostingService postingService, TransactionDetailsMapper transactionDetailsMapper, CurrencyExchangeRatesService exchangeRatesService) {
        super(depositAccountConfigService, ledgerService);
        this.entityManager = entityManager;
        this.loader = loader;
        this.depositAccountRepository = depositAccountRepository;
        this.accountStmtService = accountStmtService;
        this.postingService = postingService;
        this.transactionDetailsMapper = transactionDetailsMapper;
        this.exchangeRatesService = exchangeRatesService;
    }

    @Override
    public List<DepositAccountBO> getAccountsByIbanAndParamCurrency(String iban, String currency) {
        return depositAccountMapper.toDepositAccountListBO(depositAccountRepository.findAllByIbanAndCurrencyContaining(iban, currency));
    }

    @Override
    public DepositAccountBO getAccountByIbanAndCurrency(String iban, Currency currency) {
        return getOptionalAccountByIbanAndCurrency(iban, currency)
                       .orElseThrow(() -> DepositModuleException.builder()
                                                  .errorCode(DEPOSIT_ACCOUNT_NOT_FOUND)
                                                  .devMsg(format(MSG_IBAN_NOT_FOUND, iban, currency))
                                                  .build());
    }

    @Override
    public DepositAccountBO getAccountById(String accountId) {
        return getOptionalAccountById(accountId)
                       .orElseThrow(() -> DepositModuleException.builder()
                                                  .errorCode(DEPOSIT_ACCOUNT_NOT_FOUND)
                                                  .devMsg(format(MSG_ACCOUNT_NOT_FOUND, accountId))
                                                  .build());
    }

    @Override
    public Optional<DepositAccountBO> getOptionalAccountByIbanAndCurrency(String iban, Currency currency) {
        return depositAccountRepository.findByIbanAndCurrency(iban, getCurrencyOrEmpty(currency))
                       .map(depositAccountMapper::toDepositAccountBO);
    }

    @Override
    public Optional<DepositAccountBO> getOptionalAccountById(String accountId) {
        return depositAccountRepository.findById(accountId)
                       .map(depositAccountMapper::toDepositAccountBO);
    }

    @Override
    public DepositAccountDetailsBO getAccountDetailsByIbanAndCurrency(String iban, Currency currency, LocalDateTime refTime, boolean withBalances) {
        return getOptionalAccountByIbanAndCurrency(iban, currency)
                       .map(d -> new DepositAccountDetailsBO(d, getBalancesList(d, withBalances, refTime)))
                       .orElseThrow(() -> DepositModuleException.builder()
                                                  .errorCode(DEPOSIT_ACCOUNT_NOT_FOUND)
                                                  .devMsg(format(MSG_IBAN_NOT_FOUND, iban, currency))
                                                  .build());
    }

    @Override
    public DepositAccountDetailsBO getAccountDetailsById(String accountId, LocalDateTime refTime, boolean withBalances) {
        DepositAccountBO depositAccountBO = getDepositAccountById(accountId);
        return new DepositAccountDetailsBO(depositAccountBO, getBalancesList(depositAccountBO, withBalances, refTime));
    }

    @Override
    public TransactionDetailsBO getTransactionById(String accountId, String transactionId) {
        DepositAccountBO account = getDepositAccountById(accountId);
        LedgerAccountBO ledgerAccountBO = ledgerService.findLedgerAccountById(account.getLinkedAccounts());
        PostingLineBO line = postingService.findPostingLineById(ledgerAccountBO, transactionId);
        return transactionDetailsMapper.toTransactionSigned(line);
    }

    @Override
    public List<TransactionDetailsBO> getTransactionsByDates(String accountId, LocalDateTime dateFrom, LocalDateTime dateTo) {
        DepositAccountBO account = getDepositAccountById(accountId);
        LedgerAccountBO ledgerAccountBO = ledgerService.findLedgerAccountById(account.getLinkedAccounts());
        return postingService.findPostingsByDates(ledgerAccountBO, dateFrom, dateTo)
                       .stream()
                       .map(transactionDetailsMapper::toTransactionSigned)
                       .collect(Collectors.toList());
    }

    @Override
    public Page<TransactionDetailsBO> getTransactionsByDatesPaged(String accountId, LocalDateTime dateFrom, LocalDateTime dateTo, Pageable pageable) {
        DepositAccountBO account = getDepositAccountById(accountId);
        LedgerAccountBO ledgerAccountBO = ledgerService.findLedgerAccountById(account.getLinkedAccounts());

        return postingService.findPostingsByDatesPaged(ledgerAccountBO, dateFrom, dateTo, pageable)
                       .map(transactionDetailsMapper::toTransactionSigned);
    }

    @Override
    public boolean confirmationOfFunds(FundsConfirmationRequestBO requestBO) {
        DepositAccountDetailsBO account = getAccountDetailsByIbanAndCurrency(requestBO.getPsuAccount().getIban(), requestBO.getPsuAccount().getCurrency(), LocalDateTime.now(), true);
        Currency accountCurrency = account.getAccount().getCurrency();
        AmountBO instructedAmount = requestBO.getInstructedAmount();
        BigDecimal appliedRate = exchangeRatesService.applyRate(instructedAmount.getCurrency(), accountCurrency, instructedAmount.getAmount());
        requestBO.setInstructedAmount(new AmountBO(account.getAccount().getCurrency(), appliedRate));
        return account.getBalances().stream()
                       .filter(b -> b.getBalanceType() == INTERIM_AVAILABLE)
                       .findFirst()
                       .map(b -> isSufficientAmountAvailable(requestBO, b))
                       .orElse(Boolean.FALSE);
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
    public List<DepositAccountDetailsBO> findDetailsByBranch(String branch) {
        List<DepositAccount> accounts = depositAccountRepository.findByBranch(branch);
        List<DepositAccountBO> accountsBO = depositAccountMapper.toDepositAccountListBO(accounts);
        List<DepositAccountDetailsBO> accountDetails = new ArrayList<>();
        for (DepositAccountBO accountBO : accountsBO) {
            accountDetails.add(new DepositAccountDetailsBO(accountBO, Collections.emptyList()));
        }
        return accountDetails;
    }

    @Override
    public Page<DepositAccountDetailsBO> findDetailsByBranchPaged(String branch, String queryParam, Pageable pageable) {
        return depositAccountRepository.findByBranchAndIbanContaining(branch, queryParam, pageable)
                       .map(depositAccountMapper::toDepositAccountBO)
                       .map(d -> new DepositAccountDetailsBO(d, Collections.emptyList()));
    }

    @Override
    public void deleteTransactions(String accountId) {
        DepositAccountBO account = getAccountById(accountId);
        String linked = account.getLinkedAccounts();
        LedgerAccountBO ledgerAccount = ledgerService.findLedgerAccountById(linked);
        executeNativeQuery(POSTING_SQL, ledgerAccount.getId(), DELETE_POSTINGS_ERROR_MSG);
    }

    @Override
    public void deleteBranch(String branchId) {
        executeNativeQuery(BRANCH_SQL, branchId, DELETE_BRANCH_ERROR_MSG);
    }

    @Override
    public DepositAccountDetailsBO getDetailsByIban(String iban, LocalDateTime refTime, boolean withBalances) { //TODO This is a temporary workaround should be DELETED!!!!
        List<DepositAccountBO> accounts = getAccountsByIbanAndParamCurrency(iban, "");
        if (accounts.size() != 1) {
            throw DepositModuleException.builder()
                          .errorCode(DEPOSIT_ACCOUNT_NOT_FOUND)
                          .devMsg(format(MSG_IBAN_NOT_FOUND, iban, "EMPTY"))
                          .build();
        }
        DepositAccountBO account = accounts.iterator().next();
        return new DepositAccountDetailsBO(account, getBalancesList(account, withBalances, refTime));
    }

    @Override
    public void changeAccountsBlockedStatus(String userId, boolean isSystemBlock, boolean lockStatusToSet) {
        if (isSystemBlock) {
            depositAccountRepository.updateSystemBlockedStatus(userId, lockStatusToSet);
        } else {
            depositAccountRepository.updateBlockedStatus(userId, lockStatusToSet);
        }
    }

    @Override
    public Page<DepositAccountBO> findByBranchIdsAndMultipleParams(List<String> branchIds, String iban, Boolean blocked, Pageable pageable) {
        List<Boolean> blockedQueryParam = Optional.ofNullable(blocked)
                                                  .map(Arrays::asList)
                                                  .orElseGet(() -> Arrays.asList(true, false));
        return depositAccountRepository.findByBranchInAndIbanContainingAndBlockedInAndSystemBlockedFalse(branchIds, iban, blockedQueryParam, pageable)
                       .map(depositAccountMapper::toDepositAccountBO);
    }

    @Override
    public DepositAccountBO createNewAccount(DepositAccountBO depositAccountBO, String userName, String branch) {
        checkDepositAccountAlreadyExist(depositAccountBO);
        DepositAccount depositAccount = depositAccountMapper.toDepositAccount(depositAccountBO);
        LedgerBO ledgerBO = loadLedger();
        String depositParentAccountNbr = depositAccountConfigService.getDepositParentAccount();
        LedgerAccountBO parentLedgerAccount = new LedgerAccountBO(depositParentAccountNbr, ledgerBO);
        LedgerAccountBO ledgerAccount = new LedgerAccountBO(depositAccount.getIban(), parentLedgerAccount);
        LedgerAccountBO newLedgerAccount = ledgerService.newLedgerAccount(ledgerAccount, userName);

        depositAccount.setId(Ids.id());
        depositAccount.setName(userName);
        depositAccount.setLinkedAccounts(newLedgerAccount.getId());
        Optional.ofNullable(branch).ifPresent(depositAccount::setBranch);
        DepositAccount saved = depositAccountRepository.save(depositAccount);
        return depositAccountMapper.toDepositAccountBO(saved);
    }

    private void executeNativeQuery(String queryFilePath, String parameter, String errorMsg) {
        try {
            InputStream stream = loader.getResource(queryFilePath).getInputStream();
            String query = IOUtils.toString(stream, StandardCharsets.UTF_8);
            entityManager.createNativeQuery(query)
                    .setParameter(1, parameter)
                    .executeUpdate();
        } catch (IOException e) {
            throw DepositModuleException.builder()
                          .devMsg(format(errorMsg, parameter, e.getMessage()))
                          .errorCode(COULD_NOT_EXECUTE_STATEMENT)
                          .build();
        }
    }

    private void checkDepositAccountAlreadyExist(DepositAccountBO depositAccountBO) {
        boolean isExistingAccount = depositAccountRepository.findByIbanAndCurrency(depositAccountBO.getIban(), getCurrencyOrEmpty(depositAccountBO.getCurrency()))
                                            .isPresent();
        if (isExistingAccount) {
            String message = format("Deposit account already exists. IBAN %s. Currency %s",
                                    depositAccountBO.getIban(), depositAccountBO.getCurrency().getCurrencyCode());
            log.error(message);
            throw DepositModuleException.builder()
                          .errorCode(DEPOSIT_ACCOUNT_EXISTS)
                          .devMsg(message)
                          .build();
        }
    }

    private String getCurrencyOrEmpty(Currency currency) {
        return Optional.ofNullable(currency)
                       .map(Currency::getCurrencyCode)
                       .orElse("");
    }

    private List<BalanceBO> getBalancesList(DepositAccountBO d, boolean withBalances, LocalDateTime refTime) {
        return withBalances
                       ? getBalances(d.getLinkedAccounts(), d.getCurrency(), refTime)
                       : Collections.emptyList();
    }

    private DepositAccountBO getDepositAccountById(String accountId) {
        return depositAccountMapper.toDepositAccountBO(getDepositAccountEntityById(accountId));
    }

    private DepositAccount getDepositAccountEntityById(String accountId) {
        return depositAccountRepository.findById(accountId)
                       .orElseThrow(() -> DepositModuleException.builder()
                                                  .errorCode(DEPOSIT_ACCOUNT_NOT_FOUND)
                                                  .devMsg(format("Account with id: %s not found!", accountId))
                                                  .build());
    }

    private List<BalanceBO> getBalances(String id, Currency currency, LocalDateTime refTime) {
        LedgerBO ledger = loadLedger();
        LedgerAccountBO ledgerAccountBO = new LedgerAccountBO();
        ledgerAccountBO.setLedger(ledger);
        ledgerAccountBO.setId(id);
        return getBalances(currency, refTime, ledgerAccountBO);
    }

    private List<BalanceBO> getBalances(Currency currency, LocalDateTime refTime, LedgerAccountBO ledgerAccountBO) {
        AccountStmtBO stmt = accountStmtService.readStmt(ledgerAccountBO, refTime);
        BalanceBO interimBalance = composeBalance(currency, stmt, INTERIM_AVAILABLE);
        BalanceBO closingBalance = composeBalance(currency, stmt, CLOSING_BOOKED);
        return Arrays.asList(interimBalance, closingBalance);
    }

    private BalanceBO composeBalance(Currency currency, AccountStmtBO stmt, BalanceTypeBO balanceType) {
        BalanceBO balanceBO = new BalanceBO();
        AmountBO amount = new AmountBO(currency, stmt.creditBalance());
        balanceBO.setAmount(amount);
        balanceBO.setBalanceType(balanceType);
        balanceBO.setReferenceDate(stmt.getPstTime().toLocalDate());
        return composeFinalBalance(balanceBO, stmt);
    }

    private BalanceBO composeFinalBalance(BalanceBO balance, AccountStmtBO stmt) {
        PostingTraceBO youngestPst = stmt.getYoungestPst();
        if (youngestPst != null) {
            balance.setLastChangeDateTime(youngestPst.getSrcPstTime());
            balance.setLastCommittedTransaction(youngestPst.getSrcPstId());
        } else {
            balance.setLastChangeDateTime(stmt.getPstTime());
        }
        return balance;
    }

    private boolean isSufficientAmountAvailable(FundsConfirmationRequestBO request, BalanceBO balance) {
        AmountBO balanceAmount = balance.getAmount();
        return Optional.ofNullable(request.getInstructedAmount())
                       .map(r -> balanceAmount.getAmount().compareTo(r.getAmount()) >= 0)
                       .orElse(false);
    }
}
