package de.adorsys.ledgers.middleware.service;

import de.adorsys.ledgers.deposit.api.domain.PaymentResultBO;
import de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO;
import de.adorsys.ledgers.deposit.api.exception.PaymentNotFoundException;
import de.adorsys.ledgers.deposit.api.service.DepositAccountPaymentService;
import de.adorsys.ledgers.middleware.converter.PaymentConverter;
import de.adorsys.ledgers.middleware.service.domain.payment.PaymentResultTO;
import de.adorsys.ledgers.middleware.service.domain.payment.TransactionStatusTO;
import de.adorsys.ledgers.middleware.service.exception.PaymentNotFoundMiddlewareException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MiddlewareServiceImplTest {
    private static final String PAYMENT_ID = "myPaymentId";

    @InjectMocks
    private MiddlewareServiceImpl middlewareService;

    @Mock
    private DepositAccountPaymentService paymentService;

    @Mock
    private PaymentConverter converter;

    @SuppressWarnings("unchecked")
    @Test
    public void getPaymentStatusById() throws PaymentNotFoundMiddlewareException, PaymentNotFoundException {
        PaymentResultBO<TransactionStatusBO> paymentResultBO = mock(PaymentResultBO.class);
        PaymentResultTO<TransactionStatusTO> paymentResultTO = new PaymentResultTO<>(TransactionStatusTO.RJCT);

        when(paymentService.getPaymentStatusById(PAYMENT_ID)).thenReturn(paymentResultBO);
        when(converter.toPaymentResultTO(paymentResultBO)).thenReturn(paymentResultTO);

        PaymentResultTO<TransactionStatusTO> paymentResult = middlewareService.getPaymentStatusById(PAYMENT_ID);

        assertThat(paymentResult.getPaymentResult().getName(), is(TransactionStatusBO.RJCT.getName()));

        verify(paymentService, times(1)).getPaymentStatusById(PAYMENT_ID);
        verify(converter, times(1)).toPaymentResultTO(paymentResultBO);
    }

    @Test(expected = PaymentNotFoundMiddlewareException.class)
    public void getPaymentStatusByIdWithException() throws PaymentNotFoundMiddlewareException, PaymentNotFoundException {

        when(paymentService.getPaymentStatusById(PAYMENT_ID)).thenThrow(new PaymentNotFoundException());

        middlewareService.getPaymentStatusById(PAYMENT_ID);

        verify(paymentService, times(1)).getPaymentStatusById(PAYMENT_ID);
    }
}