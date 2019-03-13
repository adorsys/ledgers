package de.adorsys.ledgers.data.upload.service;

import de.adorsys.ledgers.data.upload.model.AccountBalance;
import de.adorsys.ledgers.data.upload.model.DataPayload;
import de.adorsys.ledgers.data.upload.model.UploadedData;
import de.adorsys.ledgers.data.upload.resource.TppDataUploadResource;
import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.api.domain.payment.AmountTO;
import de.adorsys.ledgers.middleware.api.domain.um.AccountAccessTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.client.rest.AccountMgmtStaffRestClient;
import de.adorsys.ledgers.middleware.client.rest.AuthRequestInterceptor;
import de.adorsys.ledgers.middleware.client.rest.UserMgmtStaffRestClient;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RestExecutionService {
    private static final Logger logger = LoggerFactory.getLogger(TppDataUploadResource.class);

    private final AuthRequestInterceptor authRequestInterceptor;
    private final AccountMgmtStaffRestClient accountRestClient;
    private final UserMgmtStaffRestClient userRestClient;

    public RestExecutionService(AuthRequestInterceptor authRequestInterceptor, AccountMgmtStaffRestClient accountRestClient, UserMgmtStaffRestClient userRestClient) {
        this.authRequestInterceptor = authRequestInterceptor;
        this.accountRestClient = accountRestClient;
        this.userRestClient = userRestClient;
    }

    public boolean updateLedgers(String bearerToken, DataPayload payload) {
        authRequestInterceptor.setAccessToken(bearerToken);
        boolean result = doUpdate(payload);
        authRequestInterceptor.setAccessToken(null);
        logger.info("Result of update is: {}", result ? "success" : "failure");
        return result;
    }

    private boolean doUpdate(DataPayload payload) {
        UploadedData data = initialiseDataSets(payload);
        return updateUsers(data) && updateBalances(data);
    }

    private UploadedData initialiseDataSets(DataPayload payload) {
        List<UserTO> users = Optional.ofNullable(payload.getUsers())
                                     .orElse(Collections.emptyList());
        Map<String, AccountDetailsTO> accounts = Optional.ofNullable(payload.getAccounts())
                                                         .orElse(Collections.emptyList())
                                                         .stream()
                                                         .collect(Collectors.toMap(AccountDetailsTO::getIban, a -> a));
        Map<String, AccountBalance> balances = Optional.ofNullable(payload.getBalancesList())
                                                       .orElse(Collections.emptyList())
                                                       .stream()
                                                       .collect(Collectors.toMap(AccountBalance::getIban, b -> b));
        return new UploadedData(users, accounts, balances);
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
                    .ifPresent(u -> createAccountsForUser(u.getId(), u.getAccountAccesses(), data.getDetails()));
        }
        return true;
    }

    private void createAccountsForUser(String userId, List<AccountAccessTO> accesses, Map<String, AccountDetailsTO> details) {
        accesses.stream()
                .map(access -> details.get(access.getIban()))
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
                    .ifPresent(d -> calculateDifAndUpdate(d, balances.get(d.getIban())));
        } catch (FeignException f) {
            logger.error("Could not retrieve balances for account: %s", detail.getIban());
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
                logger.error("Could not update balances for: %s with amount:", detail.getIban(), amount);
            }
        }
    }
}
