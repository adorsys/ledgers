package de.adorsys.ledgers.middleware.impl.service.upload;

import de.adorsys.ledgers.middleware.api.domain.account.AccountBalanceTO;
import de.adorsys.ledgers.middleware.api.domain.payment.AmountTO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTypeTO;
import de.adorsys.ledgers.middleware.api.domain.sca.SCAPaymentResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.um.AccountAccessTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.UploadedDataTO;
import de.adorsys.ledgers.middleware.api.service.MiddlewarePaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Currency;
import java.util.Map;
import java.util.Optional;

import static de.adorsys.ledgers.middleware.api.domain.Constants.SCOPE_FULL_ACCESS;
import static de.adorsys.ledgers.middleware.api.domain.payment.PaymentTypeTO.SINGLE;
import static de.adorsys.ledgers.middleware.impl.service.upload.ExpressionExecutionWrapper.execute;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadPaymentService {
    private final MiddlewarePaymentService middlewarePaymentService;
    private final PaymentGenerationService paymentGenerationService;

    public void uploadPayments(UploadedDataTO data, ScaInfoTO info) {
        if (data.isGeneratePayments()) {
            data.getUsers()
                    .stream()
                    .flatMap(u -> u.getAccountAccesses().stream())
                    .forEach(u -> generateAndExecutePayments(u, data, info));
        } else {
            data.getPayments()
                    .forEach(p -> execute(() -> {
                        try {
                            SCAPaymentResponseTO payment = middlewarePaymentService.initiatePayment(info, p, SINGLE);
                            info.setBearerToken(new BearerTokenTO(null, null, 0, null, null, Collections.singleton(SCOPE_FULL_ACCESS)));
                            middlewarePaymentService.executePayment(info, payment.getPaymentId());
                        } catch (Exception e) {
                            log.info("Seems payment already exist, skipping creation. {}", e.getMessage());
                        }
                    }));
        }
    }

    private void generateAndExecutePayments(AccountAccessTO access, UploadedDataTO data, ScaInfoTO info) {
        AccountBalanceTO debtorBalance = Optional.ofNullable(data.getBalances().get(access.getIban()))
                                                 .orElseGet(() -> buildAccountBalance(access.getIban(), Currency.getInstance("EUR"), BigDecimal.valueOf(100)));
        Map<PaymentTypeTO, PaymentTO> payments = paymentGenerationService.generatePayments(debtorBalance, data.getBranch());

        payments.forEach((paymentType, payment) -> execute(() -> middlewarePaymentService.initiatePayment(info, payment, paymentType)));
    }

    private AccountBalanceTO buildAccountBalance(String iban, Currency currency, BigDecimal amount) {
        AccountBalanceTO balance = new AccountBalanceTO();
        balance.setAmount(new AmountTO(currency, amount));
        balance.setIban(iban);
        return balance;
    }
}
