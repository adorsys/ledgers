package de.adorsys.ledgers.deposit.api.service.impl;

import de.adorsys.ledgers.deposit.api.domain.PaymentResultBO;
import de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO;
import de.adorsys.ledgers.deposit.api.exception.PaymentNotFoundException;
import de.adorsys.ledgers.deposit.db.domain.Payment;
import de.adorsys.ledgers.deposit.db.domain.TransactionStatus;
import de.adorsys.ledgers.deposit.db.repository.PaymentRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DepositAccountPaymentServiceImplTest {

    @InjectMocks
    private DepositAccountPaymentServiceImpl paymentService;

    @Mock
    private PaymentRepository repository;
    public static final String PAYMENT_ID = "myPaymentId";

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void getPaymentStatus() throws PaymentNotFoundException {

        TransactionStatus status = TransactionStatus.PDNG;
        Payment payment = mock(Payment.class);

        when(repository.findById(PAYMENT_ID)).thenReturn(Optional.of(payment));
        when(payment.getTransactionStatus()).thenReturn(status);

        PaymentResultBO<TransactionStatusBO> paymentResult = paymentService.getPaymentStatusById(PAYMENT_ID);

        assertThat(paymentResult.getPaymentResult().getName(), is(status.getName()));

        verify(repository, times(1)).findById(PAYMENT_ID);
        verify(payment, times(1)).getTransactionStatus();
    }

    @Test(expected = PaymentNotFoundException.class)
    public void getPaymentStatusWithException() throws PaymentNotFoundException {

        when(repository.findById(PAYMENT_ID)).thenReturn(Optional.empty());

        PaymentResultBO<TransactionStatusBO> paymentResult = paymentService.getPaymentStatusById(PAYMENT_ID);

        verify(repository, times(1)).findById(PAYMENT_ID);
    }
}