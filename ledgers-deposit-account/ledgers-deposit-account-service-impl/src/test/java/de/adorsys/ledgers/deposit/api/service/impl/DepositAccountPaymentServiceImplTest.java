/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.deposit.api.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.ledgers.deposit.api.domain.PaymentBO;
import de.adorsys.ledgers.deposit.api.domain.PaymentTypeBO;
import de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.deposit.api.service.mappers.PaymentMapper;
import de.adorsys.ledgers.deposit.db.domain.Payment;
import de.adorsys.ledgers.deposit.db.domain.TransactionStatus;
import de.adorsys.ledgers.deposit.db.repository.PaymentRepository;
import de.adorsys.ledgers.util.exception.DepositModuleException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pro.javatar.commons.reader.YamlReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepositAccountPaymentServiceImplTest {
    private static final String PAYMENT_ID = "myPaymentId";
    private static final String WRONG_PAYMENT_ID = "wrongId";
    private static final String IBAN = "DE91100000000123456789";
    private static final String ACCOUNT_ID = "accountId";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private DepositAccountPaymentServiceImpl paymentService;
    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private DepositAccountService accountService;
    @Mock
    private PaymentExecutionService executionService;

    @Test
    void getPaymentStatus() {
        // Given
        when(paymentRepository.findById(anyString())).thenReturn(Optional.of(getSinglePayment()));
        when(paymentMapper.toPaymentBO(any())).thenReturn(getSinglePaymentBO());

        // When
        TransactionStatusBO paymentResult = paymentService.getPaymentStatusById(PAYMENT_ID);

        // Then
        assertThat(paymentResult.getName(), is(TransactionStatus.RCVD.getName()));
        verify(paymentRepository, times(1)).findById(PAYMENT_ID);
    }

    @Test
    void getPaymentStatusWithException() {
        // Given
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.empty());

        // Then
        assertThrows(DepositModuleException.class, () -> paymentService.getPaymentStatusById(PAYMENT_ID));
        verify(paymentRepository, times(1)).findById(PAYMENT_ID);
    }

    @Test
    void getPaymentById() {
        testGetPaymentById(PAYMENT_ID, getSinglePayment(), getSinglePaymentBO(), false);
        testGetPaymentById(PAYMENT_ID, getBulkPayment(), getBulkPaymentBO(), false);
    }

    @Test
    void getPaymentById_not_found() {
        Payment singlePayment = getSinglePayment();
        PaymentBO singlePaymentBO = getSinglePaymentBO();
        assertThrows(DepositModuleException.class, () -> testGetPaymentById(WRONG_PAYMENT_ID, singlePayment, singlePaymentBO, true));

        Payment bulkPayment = getBulkPayment();
        PaymentBO bulkPaymentBO = getBulkPaymentBO();
        assertThrows(DepositModuleException.class, () -> testGetPaymentById(WRONG_PAYMENT_ID, bulkPayment, bulkPaymentBO, true));
    }

    @Test
    void initiatePayment() {
        // Given
        when(paymentMapper.toPayment(any())).thenReturn(getSinglePayment());
        when(paymentRepository.save(any())).thenReturn(getSinglePayment());
        when(paymentMapper.toPaymentBO(any())).thenReturn(getSinglePaymentBO());
        when(accountService.confirmationOfFunds(any())).thenReturn(true);

        // When
        PaymentBO result = paymentService.initiatePayment(getSinglePaymentBO(), TransactionStatusBO.ACTC);

        // Then
        assertNotNull(result);
    }

    @Test
    void initiatePayment_insufficientFunds() {
        // Given
        when(paymentMapper.toPayment(any())).thenReturn(getSinglePayment());
        when(accountService.confirmationOfFunds(any())).thenReturn(false);

        PaymentBO singlePaymentBO = getSinglePaymentBO();
        // Then
        assertThrows(DepositModuleException.class, () -> paymentService.initiatePayment(singlePaymentBO, TransactionStatusBO.ACTC));
    }

    @Test
    void initiatePayment_payment_already_exists() {
        // Given
        when(paymentRepository.existsById(any())).thenReturn(true);
        PaymentBO singlePaymentBO = getSinglePaymentBO();
        // Then
        assertThrows(DepositModuleException.class, () -> paymentService.initiatePayment(singlePaymentBO, TransactionStatusBO.ACTC));
    }

    @Test
    void cancelPayment() {
        // Given
        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        Payment payment = readFile(Payment.class, "PaymentSingle.yml");
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(captor.capture())).thenReturn(payment);

        // When
        paymentService.cancelPayment(PAYMENT_ID);
        // Then
        assertEquals(TransactionStatus.CANC, captor.getValue().getTransactionStatus());

        verify(paymentRepository, times(1)).findById(PAYMENT_ID);
        verify(paymentRepository, times(1)).save(any());
    }

    @Test
    void cancelPayment_Failure_pmt_nf() {
        // Given
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.empty());

        // Then
        assertThrows(DepositModuleException.class, () -> paymentService.cancelPayment(PAYMENT_ID));
        verify(paymentRepository, times(1)).findById(PAYMENT_ID);
    }

    @Test
    void cancelPayment_Failure_pmt_executed() {
        // Given
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(readFile(Payment.class, "PaymentSingleACSC.yml")));

        // Then
        assertThrows(DepositModuleException.class, () -> paymentService.cancelPayment(PAYMENT_ID));
        verify(paymentRepository, times(1)).findById(PAYMENT_ID);
    }

    @Test
    void executePayment_instant() {
        // Given
        ReflectionTestUtils.setField(paymentService, "instantPayments", getInstants());

        when(paymentRepository.findByPaymentIdAndTransactionStatus(anyString(), any())).thenReturn(Optional.of(getSinglePayment()));
        when(executionService.executePayment(any(), anyString())).thenReturn(TransactionStatusBO.ACSC);

        // When
        TransactionStatusBO result = paymentService.executePayment(PAYMENT_ID, "user");

        // Then
        assertEquals(TransactionStatusBO.ACSC, result);
    }

    @Test
    void executePayment_non_instant() {
        // Given
        when(paymentRepository.findByPaymentIdAndTransactionStatus(anyString(), any())).thenReturn(Optional.of(getSinglePayment()));
        when(executionService.schedulePayment(any())).thenReturn(TransactionStatusBO.ACTC);

        // When
        TransactionStatusBO result = paymentService.executePayment(PAYMENT_ID, "user");

        // Then
        assertEquals(TransactionStatusBO.ACTC, result);
    }

    private Set<String> getInstants() {
        HashSet<String> set = new HashSet<>();
        set.add("sepa-credit-transfers");
        return set;
    }

    @Test
    void executePayment_payment_not_found() {
        // Given
        when(paymentRepository.findByPaymentIdAndTransactionStatus(anyString(), any())).thenReturn(Optional.empty());

        // Then
        assertThrows(DepositModuleException.class, () -> paymentService.executePayment(PAYMENT_ID, "user"));
    }

    @Test
    void updatePaymentStatus() {
        // Given
        when(paymentRepository.findById(anyString())).thenReturn(Optional.of(getSinglePayment()));
        when(paymentRepository.save(any())).thenReturn(getSinglePayment());

        // When
        TransactionStatusBO result = paymentService.updatePaymentStatus(PAYMENT_ID, TransactionStatusBO.ACCC);

        // Then
        assertEquals(TransactionStatusBO.RCVD, result);
    }

    @Test
    void readIbanByPaymentId() {
        // Given
        when(paymentRepository.findById(anyString())).thenReturn(Optional.of(getSinglePayment()));

        // When
        String result = paymentService.readIbanByPaymentId(PAYMENT_ID);

        // Then
        assertEquals(IBAN, result);
    }

    @Test
    void getPaymentsByTypeStatusAndDebtor() {
        // Given
        when(paymentRepository.findAllByAccountIdInAndPaymentTypeAndTransactionStatus(anySet(), any(), any()))
                .thenReturn(Collections.singletonList(getSinglePayment()));
        when(paymentMapper.toPaymentBOList(any())).thenReturn(Collections.singletonList(getSinglePaymentBO()));

        // When
        List<PaymentBO> result = paymentService.getPaymentsByTypeStatusAndDebtor(PaymentTypeBO.SINGLE, TransactionStatusBO.ACCP, Set.of(ACCOUNT_ID));

        // Then
        assertEquals(Collections.singletonList(getSinglePaymentBO()), result);
    }

    private void testGetPaymentById(String paymentId, Payment persistedPayment, PaymentBO expectedPayment, boolean wrongIdentifier) {
        if (!wrongIdentifier) {
            when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(persistedPayment));
            when(paymentMapper.toPaymentBO(any())).thenReturn(expectedPayment);
        } else {
            when(paymentRepository.findById(WRONG_PAYMENT_ID)).thenReturn(Optional.empty());
        }

        PaymentBO result = paymentService.getPaymentById(paymentId);
        assertNotNull(result);
        assertEquals(expectedPayment, result);
        if (result.getPaymentType() == PaymentTypeBO.BULK) {
            assertEquals(2, result.getTargets().size());
        }
    }

    private Payment getSinglePayment() {
        Payment payment = readFile(Payment.class, "PaymentSingle.yml");
        payment.getTargets().forEach(t -> {
            t.setPayment(payment);
            t.setRemittanceInformationUnstructuredArray(getRemittanceInfoUnstructured());
            t.setRemittanceInformationStructuredArray(getRemittanceInfoStructured());
        });
        return payment;
    }

    private PaymentBO getSinglePaymentBO() {
        PaymentBO payment = readFile(PaymentBO.class, "PaymentSingle.yml");
        payment.getTargets().forEach(t -> {
            t.setPayment(payment);
            t.setRemittanceInformationUnstructuredArray(getRemittanceInfoUnstructured());
            t.setRemittanceInformationStructuredArray(getRemittanceInfoStructured());
        });
        return payment;
    }

    private byte[] getRemittanceInfoUnstructured() {
        List<String> remittanceUnstructuredList = Collections.singletonList("remittance");
        try {
            return objectMapper.writeValueAsBytes(remittanceUnstructuredList);
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
            throw new IllegalStateException("Resource file not found", ex);
        }
    }

    private byte[] getRemittanceInfoStructured() {
        try {
            return Files.readAllBytes(Path.of("src/test/resources/de/adorsys/ledgers/deposit/api/service/impl/RemittanceInfoStructured.yml"));
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new IllegalStateException("Resource file not found", ex);
        }
    }

    private Payment getBulkPayment() {
        Payment payment = readFile(Payment.class, "PaymentBulk.yml");
        payment.getTargets().forEach(t -> {
            t.setPayment(payment);
            t.setRemittanceInformationUnstructuredArray(getRemittanceInfoUnstructured());
            t.setRemittanceInformationStructuredArray(getRemittanceInfoStructured());
        });
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
}
