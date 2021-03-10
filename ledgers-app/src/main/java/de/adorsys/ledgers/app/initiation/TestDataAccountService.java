package de.adorsys.ledgers.app.initiation;

import de.adorsys.ledgers.app.mock.AccountBalance;
import de.adorsys.ledgers.app.mock.MockbankInitData;
import de.adorsys.ledgers.deposit.api.domain.AmountBO;
import de.adorsys.ledgers.deposit.api.domain.DepositAccountDetailsBO;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountTransactionService;
import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.service.CurrencyService;
import de.adorsys.ledgers.middleware.api.service.MiddlewareAccountManagementService;
import de.adorsys.ledgers.util.exception.DepositModuleException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Predicate;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestDataAccountService {
    private final MockbankInitData mockbankInitData;
    private final DepositAccountService depositAccountService;
    private final DepositAccountTransactionService transactionService;
    private final CurrencyService currencyService;
    private final MiddlewareAccountManagementService accountManagementService;

    public void createAccounts() {
        for (AccountDetailsTO details : mockbankInitData.getAccounts()) {
            if (!currencyService.isCurrencyValid(details.getCurrency())) {
                throw new IllegalArgumentException("Currency is not supported: " + details.getCurrency());
            }
            DepositAccountDetailsBO account;
            try {
                String userId = mockbankInitData.getUserIdByIban(details.getIban());
                details.setCreditLimit(BigDecimal.ZERO);
                accountManagementService.createDepositAccount(userId, new ScaInfoTO(), details);
            } catch (DepositModuleException e) {
                log.info("Account {} already exists, skip creation", details.getIban());
            }
            account = depositAccountService.getAccountDetailsByIbanAndCurrency(details.getIban(), details.getCurrency(), LocalDateTime.now(), true);
            updateBalanceIfRequired(details, account);
        }
    }

    private void updateBalanceIfRequired(AccountDetailsTO details, DepositAccountDetailsBO account) {
        getBalanceFromInitData(details)
                .ifPresent(b -> checkAndUpdateBalance(details, account, b));
    }

    private void checkAndUpdateBalance(AccountDetailsTO details, DepositAccountDetailsBO account, BigDecimal balanceValue) {
        AmountBO amount = new AmountBO(details.getCurrency(), balanceValue);
        try {
            if (account.getBalances().iterator().next().getAmount().getAmount().compareTo(amount.getAmount()) < 0) {
                transactionService.depositCash(account.getAccount().getId(), amount, "SYSTEM");
            }
        } catch (DepositModuleException e) {
            log.error("Unable to deposit cash to account: {} {}", details.getIban(), details.getCurrency());
        }
    }

    private Optional<BigDecimal> getBalanceFromInitData(AccountDetailsTO details) {
        return mockbankInitData.getBalances().stream()
                       .filter(getAccountBalancePredicate(details))
                       .findFirst()
                       .map(AccountBalance::getBalance);
    }

    private Predicate<AccountBalance> getAccountBalancePredicate(AccountDetailsTO details) {
        return b -> StringUtils.equals(b.getAccNbr(), details.getIban()) && b.getCurrency().equals(details.getCurrency());
    }
}
