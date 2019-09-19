package de.adorsys.ledgers.middleware.impl.service.upload;

import de.adorsys.ledgers.middleware.api.domain.account.AccountBalanceTO;
import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.api.domain.payment.AmountTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.um.UploadedDataTO;
import de.adorsys.ledgers.middleware.api.service.MiddlewareAccountManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.Map;
import java.util.Optional;

import static de.adorsys.ledgers.middleware.impl.service.upload.ExpressionExecutionWrapper.execute;

@Service
@RequiredArgsConstructor
public class UploadBalanceService {
    private final MiddlewareAccountManagementService middlewareAccountService;

    public void uploadBalances(UploadedDataTO data, ScaInfoTO info) {
        if (data.getBalances().isEmpty()) {
            return;
        }
        middlewareAccountService.listDepositAccountsByBranch(info.getUserId())
                .forEach(a -> updateBalanceIfPresent(a, data.getBalances(), info));
    }

    private void updateBalanceIfPresent(AccountDetailsTO detail, Map<String, AccountBalanceTO> balances, ScaInfoTO info) {
        Optional.ofNullable(middlewareAccountService.getDepositAccountById(detail.getId(), LocalDateTime.now(), true))
                .ifPresent(d -> calculateDifAndUpdate(d, Optional.ofNullable(balances.get(d.getIban()))
                                                                 .orElse(buildAccountBalance(d.getIban(), d.getCurrency(), BigDecimal.ZERO)), info));
    }

    private void calculateDifAndUpdate(AccountDetailsTO detail, AccountBalanceTO balance, ScaInfoTO info) {
        BigDecimal amountAtLedgers = detail.getBalances().get(0).getAmount().getAmount();
        BigDecimal requestedAmount = balance.getAmount().getAmount();

        BigDecimal delta = requestedAmount.subtract(amountAtLedgers);
        if (delta.compareTo(BigDecimal.ZERO) > 0) {
            execute(() -> middlewareAccountService.depositCash(info, detail.getId(), new AmountTO(detail.getCurrency(), delta)));
        }
    }

    private AccountBalanceTO buildAccountBalance(String iban, Currency currency, BigDecimal amount) {
        AccountBalanceTO balance = new AccountBalanceTO();
        balance.setAmount(new AmountTO(currency, amount));
        balance.setIban(iban);
        return balance;
    }
}
