/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.ledgers.deposit.api.service.impl;

import de.adorsys.ledgers.deposit.api.domain.PaymentBO;
import de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO;
import de.adorsys.ledgers.deposit.api.exception.PaymentNotFoundException;
import de.adorsys.ledgers.deposit.api.exception.PaymentProcessingException;
import de.adorsys.ledgers.deposit.api.service.DepositAccountConfigService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountPaymentService;
import de.adorsys.ledgers.deposit.api.service.PaymentSchedulerService;
import de.adorsys.ledgers.deposit.api.service.mappers.PaymentMapper;
import de.adorsys.ledgers.deposit.db.domain.Payment;
import de.adorsys.ledgers.deposit.db.domain.TransactionStatus;
import de.adorsys.ledgers.deposit.db.repository.PaymentRepository;
import de.adorsys.ledgers.postings.api.service.LedgerService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DepositAccountPaymentServiceImpl extends AbstractServiceImpl implements DepositAccountPaymentService {
    private static final String PAYMENT_EXECUTION_FAILED = "Payment execution failed due to: %s, payment id: %s";
    private static final String PAYMENT_CANCELLATION_FAILED = "Can`t cancel payment id:%s, as it is already executed";

    private final PaymentRepository paymentRepository;

    private final PaymentMapper paymentMapper;

    private final PaymentSchedulerService paymentSchedulerService;

    public DepositAccountPaymentServiceImpl(DepositAccountConfigService depositAccountConfigService, LedgerService ledgerService, PaymentRepository paymentRepository, PaymentMapper paymentMapper, PaymentSchedulerService paymentSchedulerService) {
        super(depositAccountConfigService, ledgerService);
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
        this.paymentSchedulerService = paymentSchedulerService;
    }

    @Override
    public TransactionStatusBO getPaymentStatusById(String paymentId) throws PaymentNotFoundException {
        Optional<Payment> payment = paymentRepository.findById(paymentId);
        TransactionStatus transactionStatus = payment
                                                      .map(Payment::getTransactionStatus)
                                                      .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        return TransactionStatusBO.valueOf(transactionStatus.name());
    }

    @Override
    public PaymentBO getPaymentById(String paymentId) throws PaymentNotFoundException {
        return paymentRepository.findById(paymentId)
                       .map(paymentMapper::toPaymentBO)
                       .orElseThrow(() -> new PaymentNotFoundException(paymentId));
    }

    @Override
    public PaymentBO initiatePayment(PaymentBO payment) {
        Payment persistedPayment = paymentMapper.toPayment(payment);
        persistedPayment.getTargets().forEach(t -> t.setPayment(persistedPayment));
        persistedPayment.setTransactionStatus(TransactionStatus.RCVD);
        Payment save = paymentRepository.save(persistedPayment);
        return paymentMapper.toPaymentBO(save);
    }

    /**
     * Execute a payment. This principally applies to:
     * - Single payment
     * - Future date payment
     * - Periodic Payment
     * - Bulk Payment with batch execution
     * <p>
     * + Bulk payment without batch execution will be split into single payments
     * and each single payment will be individually sent to this method.
     */
    @Override
    public TransactionStatusBO executePayment(String paymentId) throws PaymentProcessingException {
        try {
            return paymentSchedulerService.schedulePaymentExecution(paymentId);
        } catch (PaymentNotFoundException e) {
            throw new PaymentProcessingException(String.format(PAYMENT_EXECUTION_FAILED, e.getMessage(), paymentId));
        }
    }

    @Override
    public void cancelPayment(String paymentId) throws PaymentNotFoundException {
        Payment storedPayment = paymentRepository.findById(paymentId)
                                        .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        if (storedPayment.getTransactionStatus() == TransactionStatus.ACSC) {
            throw new PaymentProcessingException(String.format(PAYMENT_CANCELLATION_FAILED, paymentId));
        } else {
            storedPayment.setTransactionStatus(TransactionStatus.CANC);
            paymentRepository.save(storedPayment);
        }
    }
}
