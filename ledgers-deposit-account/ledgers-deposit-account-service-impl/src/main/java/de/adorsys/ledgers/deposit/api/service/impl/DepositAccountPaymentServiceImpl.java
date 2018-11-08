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
import de.adorsys.ledgers.deposit.api.domain.PaymentProductBO;
import de.adorsys.ledgers.deposit.api.domain.PaymentResultBO;
import de.adorsys.ledgers.deposit.api.domain.PaymentTypeBO;
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

@Service
public class DepositAccountPaymentServiceImpl extends AbstractServiceImpl implements DepositAccountPaymentService {
//    private static final Logger LOGGER = LoggerFactory.getLogger(DepositAccountServiceImpl.class);

    private final PaymentRepository paymentRepository;

    private final PaymentMapper paymentMapper;
    
    private final PaymentSchedulerService paymentSchedulerService;
    
    public DepositAccountPaymentServiceImpl(PaymentRepository paymentRepository, PaymentMapper paymentMapper, 
    		LedgerService ledgerService, DepositAccountConfigService depositAccountConfigService,
    		PaymentSchedulerService paymentSchedulerService) {
    	super(depositAccountConfigService, ledgerService);
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
        this.paymentSchedulerService = paymentSchedulerService;
    }

    @Override
    public PaymentResultBO<TransactionStatusBO> getPaymentStatusById(String paymentId) throws PaymentNotFoundException {
        Optional<Payment> payment = paymentRepository.findById(paymentId);
        TransactionStatus transactionStatus = payment
                                                      .map(Payment::getTransactionStatus)
                                                      .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        return new PaymentResultBO<>(TransactionStatusBO.valueOf(transactionStatus.name()));
    }

    @Override
    public PaymentBO getPaymentById(PaymentTypeBO paymentType, PaymentProductBO paymentProduct, String paymentId) throws PaymentNotFoundException {
        PaymentBO payment = paymentRepository.findById(paymentId)
                                    .map(paymentMapper::toPaymentBO)
                                    .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        return filterPaymentByTypeAndProduct(payment, paymentType, paymentProduct);
    }

    @Override
    public PaymentBO initiatePayment(PaymentBO payment) {
        Payment persistedPayment = paymentMapper.toPayment(payment);
        persistedPayment.getTargets().forEach(t -> t.setPayment(persistedPayment));
        Payment save = paymentRepository.save(persistedPayment);
        return paymentMapper.toPaymentBO(save);
    }

    /**
     * Execute a payment. This principally applies to:
     * - Single payment
     * - Future date payment
     * - Periodic Payment
     * - Bulk Payment with batch execution
     * 
     * + Bulk payment without batch execution will be splited into single payments
     * and each single payment will be individually sent to this method.
     */
    @Override
    public TransactionStatusBO executePayment(String paymentId, PaymentTypeBO paymentType, PaymentProductBO paymentProduct) throws PaymentNotFoundException, PaymentProcessingException {
    	
        PaymentBO storedPayment = getPaymentById(paymentType, paymentProduct, paymentId);
        if (storedPayment.getTransactionStatus() != TransactionStatusBO.RCVD) {
            throw new PaymentProcessingException("Payment execution failed due to: " + storedPayment.getTransactionStatus() + ", payment id: " + paymentId);
        }
        return paymentSchedulerService.schedulePaymentExecution(storedPayment.getPaymentId());
    }

    private PaymentBO filterPaymentByTypeAndProduct(PaymentBO payment, PaymentTypeBO paymentType, PaymentProductBO paymentProduct) throws PaymentNotFoundException {
        boolean isPresentPayment = PaymentTypeBO.valueOf(payment.getPaymentType().name()) == paymentType;
        if (isPresentPayment && payment.getPaymentType() != PaymentTypeBO.BULK) {
            isPresentPayment = payment.getTargets().stream()
                                       .map(t -> PaymentProductBO.valueOf(t.getPaymentProduct().name()))
                                       .allMatch(t -> t == paymentProduct);
        }
        if (!isPresentPayment) {
            throw new PaymentNotFoundException(payment.getPaymentId());
        }
        return payment;
    }

}
