/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.deposit.api.service.impl;

import de.adorsys.ledgers.deposit.db.domain.DepositAccount;
import de.adorsys.ledgers.deposit.db.domain.Payment;
import de.adorsys.ledgers.deposit.db.repository.DepositAccountRepository;
import de.adorsys.ledgers.deposit.db.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.javatar.commons.reader.YamlReader;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentExecutionSchedulerTest {

    private static final String ACCOUNT_ID = "accountId";
    private static final String IBAN = "DE91100000000123456789";
    private static final String SCHEDULER = "Scheduler";

    @InjectMocks
    private PaymentExecutionScheduler paymentExecutionScheduler;

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private DepositAccountRepository accountRepository;
    @Mock
    private PaymentExecutionService executionService;

    @Test
    void scheduler_executed_successfully() {
        // Given
        Payment singlePayment = getSinglePayment();
        when(paymentRepository.getAllDuePayments())
                .thenReturn(Collections.singletonList(singlePayment));
        when(accountRepository.findById(ACCOUNT_ID))
                .thenReturn(java.util.Optional.of(getDepositAccount()));
        when(accountRepository.findByIbanAndCurrency(IBAN, "EUR"))
                .thenReturn(java.util.Optional.of(getDepositAccount()));

        // When
        paymentExecutionScheduler.scheduler();

        // Then
        verify(executionService, times(1)).executePayment(singlePayment, SCHEDULER);
    }

    @Test
    void scheduler_not_executed_debtor_disabled() {
        // Given
        Payment singlePayment = getSinglePayment();
        when(paymentRepository.getAllDuePayments())
                .thenReturn(Collections.singletonList(singlePayment));
        DepositAccount blockedAccount = getDepositAccount();
        blockedAccount.setBlocked(true);

        when(accountRepository.findById(ACCOUNT_ID))
                .thenReturn(java.util.Optional.of(blockedAccount));

        // When
        paymentExecutionScheduler.scheduler();

        // Then
        verify(executionService, never()).executePayment(singlePayment, SCHEDULER);
    }

    @Test
    void scheduler_not_executed_creditor_disabled() {
        // Given
        Payment singlePayment = getSinglePayment();
        when(paymentRepository.getAllDuePayments())
                .thenReturn(Collections.singletonList(singlePayment));
        when(accountRepository.findById(anyString()))
                .thenReturn(java.util.Optional.of(getDepositAccount()));

        DepositAccount blockedAccount = getDepositAccount();
        blockedAccount.setBlocked(true);

        when(accountRepository.findByIbanAndCurrency(IBAN, "EUR"))
                .thenReturn(Optional.of(blockedAccount));
        // When
        paymentExecutionScheduler.scheduler();

        // Then
        verify(executionService, never()).executePayment(singlePayment, SCHEDULER);
    }

    private DepositAccount getDepositAccount() {
        DepositAccount activeAccount = new DepositAccount();
        activeAccount.setBlocked(false);
        activeAccount.setSystemBlocked(false);

        return activeAccount;
    }

    private Payment getSinglePayment() {
        Payment payment = readFile(Payment.class, "PaymentSingle.yml");
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