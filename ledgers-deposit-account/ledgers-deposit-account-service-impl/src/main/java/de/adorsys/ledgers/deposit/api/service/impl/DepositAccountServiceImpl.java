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
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    private final DepositAccountRepository depositAccountRepository;
    private final DepositAccountMapper depositAccountMapper = Mappers.getMapper(DepositAccountMapper.class);
    private final AccountStmtService accountStmtService;
    private final PostingService postingService;
    private final TransactionDetailsMapper transactionDetailsMapper;
    private final CurrencyExchangeRatesService exchangeRatesService;

    public DepositAccountServiceImpl(DepositAccountConfigService depositAccountConfigService,
                                     LedgerService ledgerService, DepositAccountRepository depositAccountRepository,
                                     AccountStmtService accountStmtService,
                                     PostingService postingService, TransactionDetailsMapper transactionDetailsMapper,
                                     CurrencyExchangeRatesService exchangeRatesService) {
        super(depositAccountConfigService, ledgerService);
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
        AmountBO instructedAmount = requestBO.getInstructedAmount();
        BigDecimal requestedAmountInAccountCurrency = exchangeRatesService.applyRate(instructedAmount.getCurrency(), account.getAccount().getCurrency(), instructedAmount.getAmount());
        return account.getBalances().stream()
                       .filter(b -> b.getBalanceType() == INTERIM_AVAILABLE)
                       .findFirst()
                       .map(b -> b.isSufficientAmountAvailable(requestedAmountInAccountCurrency, account.getAccount().getCreditLimit()))
                       .orElse(Boolean.FALSE);
    }

    @Override
    public String readIbanById(String id) {
        return depositAccountRepository.findById(id).map(DepositAccount::getIban).orElse(null);
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
    @Transactional
    public void changeAccountsBlockedStatus(String userId, boolean isSystemBlock, boolean lockStatusToSet) {
        if (isSystemBlock) {
            depositAccountRepository.updateSystemBlockedStatus(userId, lockStatusToSet);
        } else {
            depositAccountRepository.updateBlockedStatus(userId, lockStatusToSet);
        }
    }

    @Override
    public Page<DepositAccountBO> findByBranchIdsAndMultipleParams(Collection<String> branchIds, String iban, Boolean blocked, Pageable pageable) {
        List<Boolean> blockedQueryParam = Optional.ofNullable(blocked)
                                                  .map(Arrays::asList)
                                                  .orElseGet(() -> Arrays.asList(true, false));
        return depositAccountRepository.findByBranchInAndIbanContainingAndBlockedInAndSystemBlockedFalse(branchIds, iban, blockedQueryParam, pageable)
                       .map(depositAccountMapper::toDepositAccountBO);
    }

    @Override
    public void changeAccountsBlockedStatus(Set<String> accountIds, boolean isSystemBlock, boolean lockStatusToSet) {
        if (isSystemBlock) {
            depositAccountRepository.updateSystemBlockedStatus(accountIds, lockStatusToSet);
        } else {
            depositAccountRepository.updateBlockedStatus(accountIds, lockStatusToSet);
        }
    }

    @Override
    @Transactional
    public void changeCreditLimit(String accountId, BigDecimal creditLimit) {
        DepositAccount account = getDepositAccountEntityById(accountId);
        checkCreditLimitIsCorrect(creditLimit);
        account.setCreditLimit(creditLimit);
    }

    @Override
    public DepositAccountBO createNewAccount(DepositAccountBO depositAccountBO, String userName, String branch) {
        checkDepositAccountAlreadyExist(depositAccountBO);
        checkCreditLimitIsCorrect(depositAccountBO.getCreditLimit());
        DepositAccount depositAccount = depositAccountMapper.toDepositAccount(depositAccountBO);
        depositAccount.setId(Ids.id());
        depositAccount.setName(userName);

        LedgerAccountBO parentLedgerAccount = new LedgerAccountBO(depositAccountConfigService.getDepositParentAccount(), loadLedger());
        LedgerAccountBO ledgerAccount = new LedgerAccountBO(depositAccount.getIban(), parentLedgerAccount);

        String accountId = ledgerService.newLedgerAccount(ledgerAccount, userName).getId();
        depositAccount.setLinkedAccounts(accountId);

        Optional.ofNullable(branch).ifPresent(depositAccount::setBranch);
        DepositAccount saved = depositAccountRepository.save(depositAccount);
        return depositAccountMapper.toDepositAccountBO(saved);
    }

    private void checkCreditLimitIsCorrect(BigDecimal creditLimit) {
        if (creditLimit.signum() < 0) {
            throw DepositModuleException.builder()
                          .errorCode(UNSUPPORTED_CREDIT_LIMIT)
                          .devMsg("Credit limit value should be positive or zero")
                          .build();
        }
    }

    private void checkDepositAccountAlreadyExist(DepositAccountBO depositAccountBO) {
        boolean isExistingAccount = depositAccountRepository.findByIbanAndCurrency(depositAccountBO.getIban(), getCurrencyOrEmpty(depositAccountBO.getCurrency()))
                                            .isPresent();
        if (isExistingAccount) {
            String message = format("Deposit account already exists. IBAN %s. Currency %s",
                                    depositAccountBO.getIban(), depositAccountBO.getCurrency().getCurrencyCode());
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
}
