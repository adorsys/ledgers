package de.adorsys.ledgers.app.initiation;

import de.adorsys.ledgers.app.mock.AccountBalance;
import de.adorsys.ledgers.app.mock.BulkPaymentsData;
import de.adorsys.ledgers.app.mock.MockbankInitData;
import de.adorsys.ledgers.app.mock.SinglePaymentsData;
import de.adorsys.ledgers.deposit.api.domain.AmountBO;
import de.adorsys.ledgers.deposit.api.domain.DepositAccountBO;
import de.adorsys.ledgers.deposit.api.domain.TransactionDetailsBO;
import de.adorsys.ledgers.deposit.api.service.DepositAccountInitService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountTransactionService;
import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.api.domain.account.AccountReferenceTO;
import de.adorsys.ledgers.middleware.api.domain.payment.AmountTO;
import de.adorsys.ledgers.middleware.api.domain.payment.BulkPaymentTO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTypeTO;
import de.adorsys.ledgers.middleware.api.domain.payment.SinglePaymentTO;
import de.adorsys.ledgers.middleware.api.domain.um.AccountAccessTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.service.CurrencyService;
import de.adorsys.ledgers.middleware.impl.converter.AccountDetailsMapper;
import de.adorsys.ledgers.middleware.impl.converter.UserMapper;
import de.adorsys.ledgers.um.api.service.UserService;
import de.adorsys.ledgers.util.exception.DepositModuleException;
import de.adorsys.ledgers.util.exception.UserManagementModuleException;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@Service
public class BankInitService {
    private final Logger logger = LoggerFactory.getLogger(BankInitService.class);

    private final MockbankInitData mockbankInitData;
    private final UserService userService;
    private final UserMapper userMapper;
    private final DepositAccountInitService depositAccountInitService;
    private final DepositAccountService depositAccountService;
    private final DepositAccountTransactionService transactionService;
    private final AccountDetailsMapper accountDetailsMapper;
    private final PaymentRestInitiationService restInitiationService;
    private final CurrencyService currencyService;

    private static final String ACCOUNT_NOT_FOUND_MSG = "Account not Found! Should never happen while initiating mock data!";
    private static final String NO_USER_BY_IBAN = "Could not get User By Iban {}";
    private static final LocalDateTime START_DATE = LocalDateTime.of(2018, 1, 1, 1, 1);

    @Autowired
    public BankInitService(MockbankInitData mockbankInitData, UserService userService, UserMapper userMapper,
                           DepositAccountInitService depositAccountInitService, DepositAccountService depositAccountService,
                           DepositAccountTransactionService transactionService, AccountDetailsMapper accountDetailsMapper, PaymentRestInitiationService restInitiationService,
                           CurrencyService currencyService) {
        this.mockbankInitData = mockbankInitData;
        this.userService = userService;
        this.userMapper = userMapper;
        this.depositAccountInitService = depositAccountInitService;
        this.depositAccountService = depositAccountService;
        this.transactionService = transactionService;
        this.accountDetailsMapper = accountDetailsMapper;
        this.restInitiationService = restInitiationService;
        this.currencyService = currencyService;
    }

    public void init() {
        depositAccountInitService.initConfigData();
        createAdmin();
    }

    public void uploadTestData() {
        createUsers();
        createAccounts();
        performTransactions();
    }

    private void createAdmin() {
        try {
            userService.findByLogin("admin");
            logger.info("Admin user is already present. Skipping creation");
        } catch (UserManagementModuleException e) {
            UserTO admin = new UserTO("admin", "admin@mail.de", "admin123");
            admin.setUserRoles(Collections.singleton(UserRoleTO.SYSTEM));
            createUser(admin);
        }
    }

    private void performTransactions() {
        List<UserTO> users = mockbankInitData.getUsers();
        performSinglePayments(users);
        performBulkPayments(users);
    }

    private void performSinglePayments(List<UserTO> users) {
        for (SinglePaymentsData paymentsData : mockbankInitData.getSinglePayments()) {
            SinglePaymentTO payment = paymentsData.getSinglePayment();
            try {
                if (isAbsentTransactionRegular(payment.getDebtorAccount().getIban(), payment.getDebtorAccount().getCurrency(), payment.getEndToEndIdentification())) {
                    UserTO user = getUserByIban(users, payment.getDebtorAccount().getIban());
                    restInitiationService.executePayment(user, PaymentTypeTO.SINGLE, payment);
                }
            } catch (DepositModuleException e) {
                logger.error(ACCOUNT_NOT_FOUND_MSG);
            } catch (UserManagementModuleException e) {
                logger.error(NO_USER_BY_IBAN, payment.getDebtorAccount().getIban());
            }
        }
    }

    private void performBulkPayments(List<UserTO> users) {
        for (BulkPaymentsData paymentsData : mockbankInitData.getBulkPayments()) {
            BulkPaymentTO payment = paymentsData.getBulkPayment();
            AccountReferenceTO debtorAccount = payment.getDebtorAccount();
            try {
                boolean isAbsentTransaction;
                if (Optional.ofNullable(payment.getBatchBookingPreferred()).orElse(false)) {
                    isAbsentTransaction = isAbsentTransactionBatch(payment);
                } else {
                    isAbsentTransaction = isAbsentTransactionRegular(debtorAccount.getIban(), debtorAccount.getCurrency(), payment.getPayments().iterator().next().getEndToEndIdentification());
                }
                if (isAbsentTransaction) {
                    UserTO user = getUserByIban(users, debtorAccount.getIban());
                    restInitiationService.executePayment(user, PaymentTypeTO.BULK, payment);
                }
            } catch (DepositModuleException e) {
                logger.error(ACCOUNT_NOT_FOUND_MSG);
            } catch (UserManagementModuleException e) {
                logger.error(NO_USER_BY_IBAN, debtorAccount.getIban());
            }
        }
    }

    private boolean isAbsentTransactionBatch(BulkPaymentTO payment) {
        DepositAccountBO account = depositAccountService.getAccountByIbanAndCurrency(payment.getDebtorAccount().getIban(), payment.getDebtorAccount().getCurrency());
        List<TransactionDetailsBO> transactions = depositAccountService.getTransactionsByDates(account.getId(), START_DATE, LocalDateTime.now());
        BigDecimal total = BigDecimal.ZERO.subtract(payment.getPayments().stream()
                                                            .map(SinglePaymentTO::getInstructedAmount)
                                                            .map(AmountTO::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, 5));
        return transactions.stream()
                       .noneMatch(t -> t.getTransactionAmount().getAmount().equals(total));
    }

    private boolean isAbsentTransactionRegular(String iban, Currency currency, String entToEndId) {
        DepositAccountBO account = depositAccountService.getAccountByIbanAndCurrency(iban, currency);
        List<TransactionDetailsBO> transactions = depositAccountService.getTransactionsByDates(account.getId(), START_DATE, LocalDateTime.now());
        return transactions.stream()
                       .noneMatch(t -> entToEndId.equals(t.getEndToEndId()));
    }

    private UserTO getUserByIban(List<UserTO> users, String iban) {
        return users.stream()
                       .filter(user -> isAccountContainedInAccess(user.getAccountAccesses(), iban))
                       .findFirst()
                       .orElseThrow(() -> UserManagementModuleException.builder().build());
    }

    private void createAccounts() {
        for (AccountDetailsTO details : mockbankInitData.getAccounts()) {
            if (!currencyService.isCurrencyValid(details.getCurrency())) {
                throw new IllegalArgumentException("Currency is not supported: " + details.getCurrency());
            }
            DepositAccountBO account = depositAccountService.getOptionalAccountByIbanAndCurrency(details.getIban(), details.getCurrency())
                                               .orElseGet(() -> createAccount(details));
            updateBalanceIfRequired(details, account);
        }
    }

    private void updateBalanceIfRequired(AccountDetailsTO details, DepositAccountBO account) {
        getBalanceFromInitData(details)
                .ifPresent(b -> updateBalance(details, account, b));
    }

    private void updateBalance(AccountDetailsTO details, DepositAccountBO account, BigDecimal balanceValue) {
        AmountBO amount = new AmountBO(details.getCurrency(), balanceValue);
        try {
            transactionService.depositCash(account.getId(), amount, "SYSTEM");
        } catch (DepositModuleException e) {
            logger.error("Unable to deposit cash to account: {} {}", details.getIban(), details.getCurrency());
        }
    }

    private Optional<BigDecimal> getBalanceFromInitData(AccountDetailsTO details) {
        return mockbankInitData.getBalances().stream()
                       .filter(getAccountBalancePredicate(details))
                       .findFirst()
                       .map(AccountBalance::getBalance);
    }

    @NotNull
    private Predicate<AccountBalance> getAccountBalancePredicate(AccountDetailsTO details) {
        return b -> StringUtils.equals(b.getAccNbr(), details.getIban()) && b.getCurrency().equals(details.getCurrency());
    }

    private DepositAccountBO createAccount(AccountDetailsTO details) {
        String userName = getUserNameByIban(details.getIban());
        DepositAccountBO accountBO = accountDetailsMapper.toDepositAccountBO(details);
        return depositAccountService.createNewAccount(accountBO, userName, "");
    }

    private String getUserNameByIban(String iban) {
        return mockbankInitData.getUsers().stream()
                       .filter(u -> isAccountContainedInAccess(u.getAccountAccesses(), iban))
                       .findFirst()
                       .map(UserTO::getLogin)
                       .orElseThrow(() -> UserManagementModuleException.builder().build());
    }

    private boolean isAccountContainedInAccess(List<AccountAccessTO> access, String iban) {
        return access.stream()
                       .anyMatch(a -> a.getIban().equals(iban));
    }

    private void createUsers() {
        for (UserTO user : mockbankInitData.getUsers()) {
            try {
                userService.findByLogin(user.getLogin());
            } catch (UserManagementModuleException e) {
                user.getUserRoles().add(UserRoleTO.CUSTOMER);
                createUser(user);
            }
        }
    }

    private void createUser(UserTO user) {
        try {
            userService.create(userMapper.toUserBO(user));
        } catch (UserManagementModuleException e1) {
            logger.error("User already exists! Should never happen while initiating mock data!");
        }
    }
}
