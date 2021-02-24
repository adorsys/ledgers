package de.adorsys.ledgers.middleware.impl.service.upload;

import de.adorsys.ledgers.middleware.api.domain.account.AccountBalanceTO;
import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.api.domain.payment.AmountTO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTypeTO;
import de.adorsys.ledgers.middleware.api.domain.sca.SCAPaymentResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.um.AccessTypeTO;
import de.adorsys.ledgers.middleware.api.domain.um.AccountAccessTO;
import de.adorsys.ledgers.middleware.api.domain.um.UploadedDataTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.service.MiddlewarePaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UploadPaymentServiceTest {
    @InjectMocks
    private UploadPaymentService service;

    @Mock
    private MiddlewarePaymentService middlewarePaymentService;
    @Mock
    private PaymentGenerationService paymentGenerationService;


    @Test
    void uploadPayments_no_gen() {
        when(middlewarePaymentService.initiatePayment(any(), any())).thenReturn(new SCAPaymentResponseTO("pmtId", "ACTC", "SINGLE", null));
        service.uploadPayments(getData(false), new ScaInfoTO());
        verify(middlewarePaymentService, times(1)).initiatePayment(any(), any());
        verify(middlewarePaymentService, times(1)).executePayment(any(), any());
    }

    @Test
    void uploadPayments_gen() {
        when(paymentGenerationService.generatePayments(any(), any())).thenReturn(Map.of(PaymentTypeTO.SINGLE, new PaymentTO()));
        service.uploadPayments(getData(true), new ScaInfoTO());
        verify(middlewarePaymentService, times(1)).initiatePayment(any(), any());
    }

    private UploadedDataTO getData(boolean generatePmt) {
        return new UploadedDataTO(getUsers(), getDetails(), getBalances(), getPayments(), generatePmt, "branch");
    }

    private List<PaymentTO> getPayments() {
        return List.of(new PaymentTO());
    }

    private Map<String, AccountBalanceTO> getBalances() {
        return Map.of("", new AccountBalanceTO(new AmountTO(Currency.getInstance("EUR"), BigDecimal.TEN), null,
                                               null, null, null, "iban"));
    }

    private Map<String, AccountDetailsTO> getDetails() {
        return Map.of("iban", new AccountDetailsTO());
    }

    private List<UserTO> getUsers() {
        UserTO user = new UserTO();
        user.setAccountAccesses(List.of(new AccountAccessTO(null, "iban", Currency.getInstance("EUR"), AccessTypeTO.OWNER, 100, "id")));
        return List.of(user);
    }
}