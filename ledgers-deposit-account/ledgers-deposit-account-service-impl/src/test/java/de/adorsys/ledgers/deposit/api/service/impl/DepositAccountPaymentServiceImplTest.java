package de.adorsys.ledgers.deposit.api.service.impl;

import de.adorsys.ledgers.deposit.api.domain.*;
import de.adorsys.ledgers.deposit.api.exception.PaymentNotFoundException;
import de.adorsys.ledgers.deposit.api.service.mappers.CurrencyMapper;
import de.adorsys.ledgers.deposit.api.service.mappers.PaymentMapper;
import de.adorsys.ledgers.deposit.db.domain.Payment;
import de.adorsys.ledgers.deposit.db.domain.TransactionStatus;
import de.adorsys.ledgers.deposit.db.repository.PaymentRepository;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import pro.javatar.commons.reader.YamlReader;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DepositAccountPaymentServiceImplTest {
    private static final String PAYMENT_ID = "myPaymentId";
    private static final String WRONG_PAYMENT_ID = "wrongId";
    private static final PaymentProductBO PAYMENT_PRODUCT = PaymentProductBO.SEPA;
    private static final PaymentTypeBO PAYMENT_TYPE_SINGLE = PaymentTypeBO.SINGLE;
    private static final PaymentTypeBO PAYMENT_TYPE_BULK = PaymentTypeBO.BULK;

    @InjectMocks
    private DepositAccountPaymentServiceImpl paymentService;
    @Mock
    private PaymentMapper paymentMapper = Mappers.getMapper(PaymentMapper.class);
    @Mock
    private CurrencyMapper currencyMapper;

    @Mock
    private PaymentRepository repository;

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

    @Test
    public void getPaymentById() throws PaymentNotFoundException {
        testGetPaymentById(PAYMENT_ID, getSinglePayment(Payment.class), getSinglePayment(PaymentBO.class));
        testGetPaymentById(PAYMENT_ID, getBulkPayment(Payment.class), getBulkPayment(PaymentBO.class));
    }

    @Test(expected = PaymentNotFoundException.class)
    public void getPaymentById_not_found() throws PaymentNotFoundException {
        testGetPaymentById(WRONG_PAYMENT_ID, getSinglePayment(Payment.class), getSinglePayment(PaymentBO.class));
        testGetPaymentById(WRONG_PAYMENT_ID, getBulkPayment(Payment.class), getBulkPayment(PaymentBO.class));
    }

    private <T> void testGetPaymentById(String paymentId, Payment persistedPayment, PaymentBO expectedPayment) throws PaymentNotFoundException {
        when(repository.findById(PAYMENT_ID)).thenReturn(Optional.of(persistedPayment));
        when(repository.findById(WRONG_PAYMENT_ID)).thenReturn(Optional.empty());
        when(paymentMapper.toPaymentBO(persistedPayment)).thenReturn(expectedPayment);

        PaymentBO result = paymentService.getPaymentById(expectedPayment.getPaymentType(), PAYMENT_PRODUCT, paymentId);
        assertThat(result).isNotNull();
        assertThat(result).isEqualToComparingFieldByFieldRecursively(expectedPayment);
        if (result.getPaymentType() == PaymentTypeBO.BULK) {
            assertThat(result.getTargets().size()).isEqualTo(2);
        }
    }

    private <T> T getSinglePayment(Class<T> t) {
        return getGenericPayment(t, "PaymentSingle.yml");
    }

    private <T> T getBulkPayment(Class<T> t) {
        return getGenericPayment(t, "PaymentBulk.yml");
    }

    @Nullable
    private <T> T getGenericPayment(Class<T> t, String file) {
        try {
            return YamlReader.getInstance().getObjectFromResource(DepositAccountPaymentServiceImpl.class, file, t);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Resource file not found", e);
        }
    }
}