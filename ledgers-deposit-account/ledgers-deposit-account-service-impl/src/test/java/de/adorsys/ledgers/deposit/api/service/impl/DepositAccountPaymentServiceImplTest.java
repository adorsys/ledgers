package de.adorsys.ledgers.deposit.api.service.impl;

import de.adorsys.ledgers.deposit.api.domain.PaymentBO;
import de.adorsys.ledgers.deposit.api.domain.PaymentTypeBO;
import de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO;
import de.adorsys.ledgers.util.exception.DepositModuleException;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.deposit.api.service.mappers.PaymentMapper;
import de.adorsys.ledgers.deposit.db.domain.Payment;
import de.adorsys.ledgers.deposit.db.domain.TransactionStatus;
import de.adorsys.ledgers.deposit.db.repository.PaymentRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
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

    @InjectMocks
    private DepositAccountPaymentServiceImpl paymentService;
    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private DepositAccountService accountService;
    @Mock
    private PaymentExecutionService paymentExecutionService;

    @Test
    public void getPaymentStatus() {
        when(paymentRepository.findById(anyString())).thenReturn(Optional.of(getSinglePayment()));
        when(paymentMapper.toPaymentBO(any())).thenReturn(getSinglePaymentBO());

        TransactionStatusBO paymentResult = paymentService.getPaymentStatusById(PAYMENT_ID);

        assertThat(paymentResult.getName(), is(TransactionStatus.RCVD.getName()));
        verify(paymentRepository, times(1)).findById(PAYMENT_ID);
    }

    @Test(expected = DepositModuleException.class)
    public void getPaymentStatusWithException() {

        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.empty());

        TransactionStatusBO paymentResult = paymentService.getPaymentStatusById(PAYMENT_ID);

        verify(paymentRepository, times(1)).findById(PAYMENT_ID);
    }

    @Test
    public void getPaymentById() {
        testGetPaymentById(PAYMENT_ID, getSinglePayment(), getSinglePaymentBO());
        testGetPaymentById(PAYMENT_ID, getBulkPayment(), getBulkPaymentBO());
    }

    @Test(expected = DepositModuleException.class)
    public void getPaymentById_not_found() {
        testGetPaymentById(WRONG_PAYMENT_ID, getSinglePayment(), getSinglePaymentBO());
        testGetPaymentById(WRONG_PAYMENT_ID, getBulkPayment(), getBulkPaymentBO());
    }

    @Test
    public void initiatePayment() {
        when(paymentMapper.toPayment(any())).thenReturn(getSinglePayment());
        when(paymentRepository.save(any())).thenReturn(getSinglePayment());
        when(paymentMapper.toPaymentBO(any())).thenReturn(getSinglePaymentBO());
        when(accountService.confirmationOfFunds(any())).thenReturn(true);

        PaymentBO result = paymentService.initiatePayment(getSinglePaymentBO(), TransactionStatusBO.ACTC);
        assertThat(result).isNotNull();
    }

    @Test(expected = DepositModuleException.class)
    public void initiatePayment_insufficientFunds() {
        when(paymentMapper.toPayment(any())).thenReturn(getSinglePayment());
        when(accountService.confirmationOfFunds(any())).thenReturn(false);

        paymentService.initiatePayment(getSinglePaymentBO(), TransactionStatusBO.ACTC);
    }

    private void testGetPaymentById(String paymentId, Payment persistedPayment, PaymentBO expectedPayment) {
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(persistedPayment));
        when(paymentRepository.findById(WRONG_PAYMENT_ID)).thenReturn(Optional.empty());
        when(paymentMapper.toPaymentBO(any())).thenReturn(expectedPayment);

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
    public void cancelPayment() {
        //Given
        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        Payment payment = readFile(Payment.class, "PaymentSingle.yml");
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(captor.capture())).thenReturn(payment);

        //When
        paymentService.cancelPayment(PAYMENT_ID);
        //Than
        assertThat(captor.getValue().getTransactionStatus()).isEqualTo(TransactionStatus.CANC);

        verify(paymentRepository, times(1)).findById(PAYMENT_ID);
        verify(paymentRepository, times(1)).save(any());
    }

    @Test(expected = DepositModuleException.class)
    public void cancelPayment_Failure_pmt_nf() {
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.empty());

        //When
        paymentService.cancelPayment(PAYMENT_ID);

        verify(paymentRepository, times(1)).findById(PAYMENT_ID);
    }

    @Test(expected = DepositModuleException.class)
    public void cancelPayment_Failure_pmt_executed() {
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(readFile(Payment.class, "PaymentSingleACSC.yml")));

        //When
        paymentService.cancelPayment(PAYMENT_ID);

        verify(paymentRepository, times(1)).findById(PAYMENT_ID);
    }
}
