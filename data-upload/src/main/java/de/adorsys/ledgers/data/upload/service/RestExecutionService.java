package de.adorsys.ledgers.data.upload.service;

import de.adorsys.ledgers.data.upload.model.AccountBalance;
import de.adorsys.ledgers.data.upload.model.DataPayload;
import de.adorsys.ledgers.data.upload.model.UploadedData;
import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.api.domain.payment.AmountTO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTypeTO;
import de.adorsys.ledgers.middleware.api.domain.um.AccountAccessTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.client.rest.*;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RestExecutionService {
    private static final Logger logger = LoggerFactory.getLogger(RestExecutionService.class);

    private final TestsDataGenerationService generationService;
    private final AuthRequestInterceptor authRequestInterceptor;
    private final AccountMgmtStaffRestClient accountRestClient;
    private final UserMgmtStaffRestClient userRestClient;
    private final UserMgmtRestClient userMgmtRestClient;
    private final PaymentRestClient paymentRestClient;

    public RestExecutionService(TestsDataGenerationService generationService, AuthRequestInterceptor authRequestInterceptor, AccountMgmtStaffRestClient accountRestClient, UserMgmtStaffRestClient userRestClient, UserMgmtRestClient userMgmtRestClient, PaymentRestClient paymentRestClient) {
        this.generationService = generationService;
        this.authRequestInterceptor = authRequestInterceptor;
        this.accountRestClient = accountRestClient;
        this.userRestClient = userRestClient;
        this.userMgmtRestClient = userMgmtRestClient;
        this.paymentRestClient = paymentRestClient;
    }

    public String getBranchName(String accessToken) {
        authRequestInterceptor.setAccessToken(accessToken);
        String branchName = null;
        try {
            branchName = Objects.requireNonNull(userMgmtRestClient.getUser().getBody()).getBranch();
        } catch (Exception e) {
            logger.error("Could not get Response from Ledgers querying for Introspect Token");
        }
        return branchName;
    }

    public boolean updateLedgers(String bearerToken, DataPayload payload) {
        authRequestInterceptor.setAccessToken(bearerToken);
        boolean result = doUpdate(payload, bearerToken);
        authRequestInterceptor.setAccessToken(null);
        logger.info("Result of update is: {}", result ? "success" : "failure");
        return result;
    }

    private boolean doUpdate(DataPayload payload, String token) {
        UploadedData data = initialiseDataSets(payload, token);
        return updateUsers(data) && updateBalances(data) && performPayments(data);
    }

    private boolean performPayments(UploadedData data) {
        if (data.isGeneratePayments()) {
            return data.getUsers().stream()
                           .allMatch(u -> performPaymentsForUser(u, data));
        } else {
            return true;
        }
    }

    private boolean performPaymentsForUser(UserTO user, UploadedData data) {
        return user.getAccountAccesses().stream()
                       .allMatch(a -> generateAndExecutePayments(a, data));
    }

    private boolean generateAndExecutePayments(AccountAccessTO access, UploadedData data) {
        AccountBalance debtorBalance = Optional.ofNullable(data.getBalances().get(access.getIban()))
                                               .orElseGet(() -> new AccountBalance(null, access.getIban(), Currency.getInstance("EUR"), BigDecimal.valueOf(100)));
        Map<PaymentTypeTO, Object> payments = generationService.generatePayments(debtorBalance, data.getBranch());

        return payments.entrySet().stream()
                       .allMatch(this::performRestPaymentExecute);
    }

    private boolean performRestPaymentExecute(Map.Entry<PaymentTypeTO, Object> entry) {
        try {
            paymentRestClient.initiatePayment(entry.getKey(), entry.getValue());
            return true;
        } catch (Exception e) {
            logger.error("{} with body {} failed for some reason", entry.getKey(), entry.getValue());
            return false;
        }
    }

    private UploadedData initialiseDataSets(DataPayload payload, String token) {
        List<UserTO> users = Optional.ofNullable(payload.getUsers())
                                     .orElse(Collections.emptyList());
        Map<String, AccountDetailsTO> accounts = getAccountsForUploadedData(payload);
        Map<String, AccountBalance> balances = getBalancesForUploadedData(payload);
        String branch = Optional.ofNullable(payload.getBranch())
                                .orElseGet(() -> getBranchName(token));

        return new UploadedData(users, accounts, balances, payload.isGeneratePayments(), branch);
    }

    private Map<String, AccountBalance> getBalancesForUploadedData(DataPayload payload) {
        return Optional.ofNullable(payload.getBalancesList())
                       .orElse(Collections.emptyList())
                       .stream()
                       .collect(Collectors.toMap(AccountBalance::getIban, b -> b));
    }

    private Map<String, AccountDetailsTO> getAccountsForUploadedData(DataPayload payload) {
        return Optional.ofNullable(payload.getAccounts())
                       .orElse(Collections.emptyList())
                       .stream()
                       .collect(Collectors.toMap(AccountDetailsTO::getIban, a -> a));
    }

    private boolean updateUsers(UploadedData data) {
        for (UserTO user : data.getUsers()) {
            try {
                user = userRestClient.createUser(user).getBody();
            } catch (FeignException f) {
                String msg = String.format("User: %s probably already exists", user.getLogin());
                if (f.status() == 500 || f.status() == 403) {
                    msg = String.format("Connection problem %s", f.getMessage());
                    logger.error(msg);
                    return false;
                }
                logger.error(msg);
            }
            Optional.ofNullable(user)
                    .ifPresent(u -> {
                        if (!data.getDetails().isEmpty()) {
                            createAccountsForUser(u.getId(), u.getAccountAccesses(), data.getDetails());
                        }
                    });
        }
        return true;
    }

    private void createAccountsForUser(String userId, List<AccountAccessTO> accesses, Map<String, AccountDetailsTO> details) {
        accesses.stream()
                .filter(access -> details.containsKey(access.getIban()))
                .map(a -> details.get(a.getIban()))
                .forEach(account -> createAccount(userId, account));
    }

    private void createAccount(String userId, AccountDetailsTO account) {
        try {
            accountRestClient.createDepositAccountForUser(userId, account);
        } catch (FeignException f) {
            logger.error("Account: {} {} creation error, probably it already exists", account.getIban(), account.getCurrency());
        }
    }

    private boolean updateBalances(UploadedData data) {
        if (data.getBalances().isEmpty()) {
            return true;
        }
        try {
            List<AccountDetailsTO> accountsAtLedgers = Optional.ofNullable(accountRestClient.getListOfAccounts().getBody())
                                                               .orElse(Collections.emptyList());
            accountsAtLedgers
                    .forEach(a -> updateBalanceIfPresent(a, data.getBalances()));
            return true;
        } catch (FeignException e) {
            logger.error("Could not retrieve accounts from Ledgers");
            return false;
        }
    }

    private void updateBalanceIfPresent(AccountDetailsTO detail, Map<String, AccountBalance> balances) {
        try {
            Optional.ofNullable(accountRestClient.getAccountDetailsById(detail.getId()).getBody())
                    .ifPresent(d -> calculateDifAndUpdate(d, Optional.ofNullable(balances.get(d.getIban()))
                                                                     .orElse(getZeroBalance(d))));
        } catch (FeignException f) {
            logger.error("Could not retrieve balances for account: {}", detail.getIban());
        }
    }

    private void calculateDifAndUpdate(AccountDetailsTO detail, AccountBalance balance) {
        BigDecimal amountAtLedgers = detail.getBalances().get(0).getAmount().getAmount();
        BigDecimal requestedAmount = balance.getAmount();

        BigDecimal delta = requestedAmount.subtract(amountAtLedgers);
        if (delta.compareTo(BigDecimal.ZERO) > 0) {
            AmountTO amount = new AmountTO();
            amount.setCurrency(detail.getCurrency());
            amount.setAmount(delta);
            try {
                accountRestClient.depositCash(detail.getId(), amount);
            } catch (FeignException f) {
                logger.error("Could not update balances for: {} with amount: {}", detail.getIban(), amount);
            }
        }
    }

    private AccountBalance getZeroBalance(AccountDetailsTO details) {
        return new AccountBalance(null, details.getIban(), details.getCurrency(), BigDecimal.ZERO);
    }
}
