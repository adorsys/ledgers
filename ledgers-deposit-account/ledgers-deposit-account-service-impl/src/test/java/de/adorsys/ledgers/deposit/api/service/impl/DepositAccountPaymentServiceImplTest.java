package de.adorsys.ledgers.deposit.api.service.impl;

import de.adorsys.ledgers.deposit.api.domain.PaymentBO;
import de.adorsys.ledgers.deposit.api.domain.PaymentProductBO;
import de.adorsys.ledgers.deposit.api.domain.PaymentTypeBO;
import de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO;
import de.adorsys.ledgers.deposit.api.exception.PaymentNotFoundException;
import de.adorsys.ledgers.deposit.api.exception.PaymentProcessingException;
import de.adorsys.ledgers.deposit.api.service.mappers.PaymentMapper;
import de.adorsys.ledgers.deposit.db.domain.Payment;
import de.adorsys.ledgers.deposit.db.domain.TransactionStatus;
import de.adorsys.ledgers.deposit.db.repository.PaymentRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import pro.javatar.commons.reader.YamlReader;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
    private PaymentRepository paymentRepository;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void getPaymentStatus() throws PaymentNotFoundException {
        when(paymentRepository.findById(any())).thenReturn(Optional.of(getSinglePayment()));

        TransactionStatusBO paymentResult = paymentService.getPaymentStatusById(PAYMENT_ID);

        assertThat(paymentResult.getName(), is(TransactionStatus.RCVD.getName()));
        verify(paymentRepository, times(1)).findById(PAYMENT_ID);
    }

    @Test(expected = PaymentNotFoundException.class)
    public void getPaymentStatusWithException() throws PaymentNotFoundException {

        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.empty());

        TransactionStatusBO paymentResult = paymentService.getPaymentStatusById(PAYMENT_ID);

        verify(paymentRepository, times(1)).findById(PAYMENT_ID);
    }

    @Test
    public void getPaymentById() throws PaymentNotFoundException {
        testGetPaymentById(PAYMENT_ID, getSinglePayment(), getSinglePaymentBO());
        testGetPaymentById(PAYMENT_ID, getBulkPayment(), getBulkPaymentBO());
    }

    @Test(expected = PaymentNotFoundException.class)
    public void getPaymentById_not_found() throws PaymentNotFoundException {
        testGetPaymentById(WRONG_PAYMENT_ID, getSinglePayment(), getSinglePaymentBO());
        testGetPaymentById(WRONG_PAYMENT_ID, getBulkPayment(), getBulkPaymentBO());
    }

    @Test
    public void initiatePayment() {
        when(paymentMapper.toPayment(any())).thenReturn(getSinglePayment());
        when(paymentRepository.save(any())).thenReturn(getSinglePayment());
        when(paymentMapper.toPaymentBO(any())).thenReturn(getSinglePaymentBO());

        PaymentBO result = paymentService.initiatePayment(getSinglePaymentBO(), TransactionStatusBO.ACTC);
        assertThat(result).isNotNull();
    }

    private <T> void testGetPaymentById(String paymentId, Payment persistedPayment, PaymentBO expectedPayment) throws PaymentNotFoundException {
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(persistedPayment));
        when(paymentRepository.findById(WRONG_PAYMENT_ID)).thenReturn(Optional.empty());
        when(paymentMapper.toPaymentBO(persistedPayment)).thenReturn(expectedPayment);

        PaymentBO result = paymentService.getPaymentById(paymentId);
        assertThat(result).isNotNull();
        assertThat(result).isEqualToComparingFieldByFieldRecursively(expectedPayment);
        if (result.getPaymentType() == PaymentTypeBO.BULK) {
            assertThat(result.getTargets().size()).isEqualTo(2);
        }
    }

    private Payment getSinglePayment() {
        Payment payment = readFile(Payment.class, "PaymentSingle.yml");
        payment.getTargets().forEach(t -> t.setPayment(payment));
        return payment;
    }

    private PaymentBO getSinglePaymentBO() {
        PaymentBO payment = readFile(PaymentBO.class, "PaymentSingle.yml");
        payment.getTargets().forEach(t -> t.setPayment(payment));
        return payment;
    }

    private Payment getBulkPayment() {
        Payment payment = readFile(Payment.class, "PaymentBulk.yml");
        payment.getTargets().forEach(t -> t.setPayment(payment));
        return payment;
    }

    private PaymentBO getBulkPaymentBO() {
        PaymentBO payment = readFile(PaymentBO.class, "PaymentBulk.yml");
        payment.getTargets().forEach(t -> t.setPayment(payment));
        return payment;
    }

    private <T> T readFile(Class<T> t, String file) {
        try {
            return YamlReader.getInstance().getObjectFromResource(DepositAccountPaymentServiceImpl.class, file, t);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Resource file not found", e);
        }
    }

    @Test
    public void cancelPayment() throws PaymentNotFoundException {
        //Given
        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);

        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(readFile(Payment.class, "PaymentSingle.yml")));
        when(paymentRepository.save(captor.capture())).thenReturn(mock(Payment.class));

        //When
        paymentService.cancelPayment(PAYMENT_ID);
        //Than
        assertThat(captor.getValue().getTransactionStatus()).isEqualTo(TransactionStatus.CANC);

        verify(paymentRepository, times(1)).findById(PAYMENT_ID);
        verify(paymentRepository, times(1)).save(any());
    }

    @Test(expected = PaymentNotFoundException.class)
    public void cancelPayment_Failure_pmt_nf() throws PaymentNotFoundException {
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.empty());

        //When
        paymentService.cancelPayment(PAYMENT_ID);

        verify(paymentRepository, times(1)).findById(PAYMENT_ID);
    }

    @Test(expected = PaymentProcessingException.class)
    public void cancelPayment_Failure_pmt_executed() throws PaymentNotFoundException {
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(readFile(Payment.class, "PaymentSingleACSC.yml")));

        //When
        paymentService.cancelPayment(PAYMENT_ID);

        verify(paymentRepository, times(1)).findById(PAYMENT_ID);
    }
}