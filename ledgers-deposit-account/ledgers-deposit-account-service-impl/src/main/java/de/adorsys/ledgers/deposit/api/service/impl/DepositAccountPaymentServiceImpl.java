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

import java.util.Optional;

import org.springframework.stereotype.Service;

import de.adorsys.ledgers.deposit.api.domain.PaymentBO;
import de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO;
import de.adorsys.ledgers.deposit.api.exception.PaymentNotFoundException;
import de.adorsys.ledgers.deposit.api.exception.PaymentProcessingException;
import de.adorsys.ledgers.deposit.api.service.DepositAccountConfigService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountPaymentService;
import de.adorsys.ledgers.deposit.api.service.mappers.PaymentMapper;
import de.adorsys.ledgers.deposit.db.domain.Payment;
import de.adorsys.ledgers.deposit.db.domain.TransactionStatus;
import de.adorsys.ledgers.deposit.db.repository.PaymentRepository;
import de.adorsys.ledgers.postings.api.service.LedgerService;

@Service
public class DepositAccountPaymentServiceImpl extends AbstractServiceImpl implements DepositAccountPaymentService {
    private static final String PAYMENT_EXECUTION_FAILED = "Payment execution failed due to: %s, payment id: %s";
    private static final String PAYMENT_CANCELLATION_FAILED = "Can`t cancel payment id:%s, as it is already executed";

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final PaymentExecutionService executionService;

    public DepositAccountPaymentServiceImpl(DepositAccountConfigService depositAccountConfigService,
                                            LedgerService ledgerService, PaymentRepository paymentRepository,
                                            PaymentMapper paymentMapper, PaymentExecutionService executionService) {
        super(depositAccountConfigService, ledgerService);
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
        this.executionService = executionService;
    }

    @Override
    public TransactionStatusBO getPaymentStatusById(String paymentId) throws PaymentNotFoundException {
        Optional<Payment> payment = paymentRepository.findById(paymentId);
        TransactionStatus transactionStatus = payment.map(Payment::getTransactionStatus)
                                                      .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        return TransactionStatusBO.valueOf(transactionStatus.name());
    }

    @Override
    public PaymentBO getPaymentById(String paymentId) throws PaymentNotFoundException {
        return paymentRepository.findById(paymentId).map(paymentMapper::toPaymentBO)
                       .orElseThrow(() -> new PaymentNotFoundException(paymentId));
    }

    @Override
    public PaymentBO initiatePayment(PaymentBO payment, TransactionStatusBO status) {
        Payment persistedPayment = paymentMapper.toPayment(payment);
        persistedPayment.getTargets().forEach(t -> t.setPayment(persistedPayment));
        persistedPayment.setTransactionStatus(TransactionStatus.valueOf(status.name()));
        Payment savedPayment = paymentRepository.save(persistedPayment);
        return paymentMapper.toPaymentBO(savedPayment);
    }

    /**
     * Execute a payment. This principally applies to: - Single payment - Future
     * date payment - Periodic Payment - Bulk Payment with batch execution
     * <p>
     * + Bulk payment without batch execution will be split into single payments and
     * each single payment will be individually sent to this method.
     */
    @Override
    public TransactionStatusBO executePayment(String paymentId, String userName) throws PaymentProcessingException {
        Optional<Payment> payment = paymentRepository.findByPaymentIdAndTransactionStatus(paymentId, TransactionStatus.ACTC);
        if (payment.isPresent()) {
            Payment pmt = payment.get();
            return pmt.isInstant()
                           ? executionService.executePayment(pmt, userName)
                           : executionService.schedulePayment(pmt);
        }
        throw new PaymentProcessingException(String.format(PAYMENT_EXECUTION_FAILED, "Payment not found", paymentId));

    }

    @Override
    public TransactionStatusBO cancelPayment(String paymentId) throws PaymentNotFoundException {
        Payment storedPayment = paymentRepository.findById(paymentId)
                                        .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        if (storedPayment.getTransactionStatus() == TransactionStatus.ACSC) {
            throw new PaymentProcessingException(String.format(PAYMENT_CANCELLATION_FAILED, paymentId));
        }
        Payment p = updatePaymentStatus(storedPayment, TransactionStatus.CANC);
        
        return TransactionStatusBO.valueOf(p.getTransactionStatus().name());
        
    }

    @Override
    public String readIbanByPaymentId(String paymentId) {
        return paymentRepository.findById(paymentId).map(p -> p.getDebtorAccount().getIban())
                       .orElse(null);
    }

    @Override
    public TransactionStatusBO updatePaymentStatusToAuthorised(String paymentId) throws PaymentNotFoundException {
    	Payment payment = paymentRepository.findByPaymentId(paymentId)
    		.orElseThrow(() -> new PaymentNotFoundException(paymentId));
        Payment p = updatePaymentStatus(payment, TransactionStatus.ACTC);
        return TransactionStatusBO.valueOf(p.getTransactionStatus().name());
    }

    private Payment updatePaymentStatus(Payment payment, TransactionStatus updatedStatus) {
        payment.setTransactionStatus(updatedStatus);
        return paymentRepository.save(payment);
    }
}
