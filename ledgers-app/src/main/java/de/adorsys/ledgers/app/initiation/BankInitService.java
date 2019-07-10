package de.adorsys.ledgers.app.initiation;

import de.adorsys.ledgers.app.mock.AccountBalance;
import de.adorsys.ledgers.app.mock.BulkPaymentsData;
import de.adorsys.ledgers.app.mock.MockbankInitData;
import de.adorsys.ledgers.app.mock.SinglePaymentsData;
import de.adorsys.ledgers.deposit.api.domain.AmountBO;
import de.adorsys.ledgers.deposit.api.domain.DepositAccountBO;
import de.adorsys.ledgers.deposit.api.domain.DepositAccountDetailsBO;
import de.adorsys.ledgers.deposit.api.domain.TransactionDetailsBO;
import de.adorsys.ledgers.deposit.api.exception.DepositAccountNotFoundException;
import de.adorsys.ledgers.deposit.api.service.DepositAccountInitService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.api.domain.payment.AmountTO;
import de.adorsys.ledgers.middleware.api.domain.payment.BulkPaymentTO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTypeTO;
import de.adorsys.ledgers.middleware.api.domain.payment.SinglePaymentTO;
import de.adorsys.ledgers.middleware.api.domain.um.AccountAccessTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.impl.converter.AccountDetailsMapper;
import de.adorsys.ledgers.middleware.impl.converter.UserMapper;
import de.adorsys.ledgers.um.api.exception.UserManagementModuleException;
import de.adorsys.ledgers.um.api.service.UserService;
import org.apache.commons.lang3.StringUtils;
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

@Service
public class BankInitService {
    private final Logger logger = LoggerFactory.getLogger(BankInitService.class);

    private final MockbankInitData mockbankInitData;
    private final UserService userService;
    private final UserMapper userMapper;
    private final DepositAccountInitService depositAccountInitService;
    private final DepositAccountService depositAccountService;
    private final AccountDetailsMapper accountDetailsMapper;
    private final PaymentRestInitiationService restInitiationService;

    private static final String ACCOUNT_NOT_FOUND_MSG = "Account not Found! Should never happen while initiating mock data!";
    private static final String NO_USER_BY_IBAN = "Could not get User By Iban {}";
    private static final LocalDateTime START_DATE = LocalDateTime.of(2018, 1, 1, 1, 1);

    @Autowired
    public BankInitService(MockbankInitData mockbankInitData, UserService userService, UserMapper userMapper,
                           DepositAccountInitService depositAccountInitService, DepositAccountService depositAccountService,
                           AccountDetailsMapper accountDetailsMapper, PaymentRestInitiationService restInitiationService) {
        this.mockbankInitData = mockbankInitData;
        this.userService = userService;
        this.userMapper = userMapper;
        this.depositAccountInitService = depositAccountInitService;
        this.depositAccountService = depositAccountService;
        this.accountDetailsMapper = accountDetailsMapper;
        this.restInitiationService = restInitiationService;
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
            logger.error("Admin user is already present. Skipping creation");
        } catch (UserManagementModuleException e) { //TODO GET RID of Exception Driven Logic https://git.adorsys.de/adorsys/xs2a/psd2-dynamic-sandbox/issues/211
            UserTO admin = new UserTO("admin", "admin@mail.de", "admin123"); //TODO Matter of refactoring
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
                if (isAbsentTransactionRegular(payment.getDebtorAccount().getIban(), payment.getEndToEndIdentification())) {
                    UserTO user = getUserByIban(users, payment.getDebtorAccount().getIban());
                    restInitiationService.executePayment(user, PaymentTypeTO.SINGLE, payment);
                }
            } catch (DepositAccountNotFoundException e) {
                logger.error(ACCOUNT_NOT_FOUND_MSG);
            } catch (UserManagementModuleException e) {
                logger.error(NO_USER_BY_IBAN, payment.getDebtorAccount().getIban());
            }
        }
    }

    private void performBulkPayments(List<UserTO> users) {
        for (BulkPaymentsData paymentsData : mockbankInitData.getBulkPayments()) {
            BulkPaymentTO payment = paymentsData.getBulkPayment();
            try {
                boolean isAbsentTransaction;
                if (payment.getBatchBookingPreferred()) {
                    isAbsentTransaction = isAbsentTransactionBatch(payment);
                } else {
                    isAbsentTransaction = isAbsentTransactionRegular(payment.getDebtorAccount().getIban(), payment.getPayments().iterator().next().getEndToEndIdentification());
                }
                if (isAbsentTransaction) {
                    UserTO user = getUserByIban(users, payment.getDebtorAccount().getIban());
                    restInitiationService.executePayment(user, PaymentTypeTO.BULK, payment);
                }
            } catch (DepositAccountNotFoundException e) {
                logger.error(ACCOUNT_NOT_FOUND_MSG);
            } catch (UserManagementModuleException e) {
                logger.error(NO_USER_BY_IBAN, payment.getDebtorAccount().getIban());
            }
        }
    }

    private boolean isAbsentTransactionBatch(BulkPaymentTO payment) {
        boolean isAbsentTransaction;
        DepositAccountDetailsBO account = depositAccountService.getDepositAccountByIban(payment.getDebtorAccount().getIban(), LocalDateTime.now(), false);
        List<TransactionDetailsBO> transactions = depositAccountService.getTransactionsByDates(account.getAccount().getId(), START_DATE, LocalDateTime.now());
        BigDecimal total = BigDecimal.ZERO.subtract(payment.getPayments().stream()
                                                            .map(SinglePaymentTO::getInstructedAmount)
                                                            .map(AmountTO::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, 5));
        isAbsentTransaction = transactions.stream()
                                      .noneMatch(t -> t.getTransactionAmount().getAmount().equals(total));
        return isAbsentTransaction;
    }

    private boolean isAbsentTransactionRegular(String iban, String entToEndId) {
        DepositAccountDetailsBO account = depositAccountService.getDepositAccountByIban(iban, LocalDateTime.now(), false);
        List<TransactionDetailsBO> transactions = depositAccountService.getTransactionsByDates(account.getAccount().getId(), START_DATE, LocalDateTime.now());
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
            try {
                depositAccountService.getDepositAccountByIban(details.getIban(), LocalDateTime.now(), false);
            } catch (DepositAccountNotFoundException e) {
                createAccount(details)      //TODO Matter of refactoring
                        .ifPresent(a -> updateBalanceIfRequired(details, a));
            }
        }
    }

    private void updateBalanceIfRequired(AccountDetailsTO details, DepositAccountBO a) {
        getBalanceFromInitData(details)
                .ifPresent(b -> updateBalance(details, a, b));
    }

    private void updateBalance(AccountDetailsTO details, DepositAccountBO a, BigDecimal b) {
        AmountBO amount = new AmountBO(Currency.getInstance("EUR"), b);
        try {
            depositAccountService.depositCash(a.getId(), amount, "SYSTEM");
        } catch (DepositAccountNotFoundException e) {
            logger.error("Unable to deposit cash to account: {}", details.getIban());
        }
    }

    private Optional<BigDecimal> getBalanceFromInitData(AccountDetailsTO details) {
        return mockbankInitData.getBalances().stream()
                       .filter(b -> StringUtils.equals(b.getAccNbr(), details.getIban()))
                       .findFirst()
                       .map(AccountBalance::getBalance);
    }

    private Optional<DepositAccountBO> createAccount(AccountDetailsTO details) {
        try {
            String userName = getUserNameByIban(details.getIban());
            DepositAccountBO accountBO = accountDetailsMapper.toDepositAccountBO(details);
            return Optional.of(depositAccountService.createDepositAccount(accountBO, userName));
        } catch (DepositAccountNotFoundException e) {
            logger.error("Error creating Account For Mocked User");
            return Optional.empty();
        }
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
