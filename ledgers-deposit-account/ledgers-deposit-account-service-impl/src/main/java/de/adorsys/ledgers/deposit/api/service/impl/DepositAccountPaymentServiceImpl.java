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

import de.adorsys.ledgers.deposit.api.domain.*;
import de.adorsys.ledgers.deposit.api.exception.*;
import de.adorsys.ledgers.deposit.api.service.DepositAccountConfigService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountPaymentService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.deposit.api.service.mappers.PaymentMapper;
import de.adorsys.ledgers.deposit.db.domain.Payment;
import de.adorsys.ledgers.deposit.db.domain.TransactionStatus;
import de.adorsys.ledgers.deposit.db.repository.PaymentRepository;
import de.adorsys.ledgers.postings.api.service.LedgerService;
import de.adorsys.ledgers.util.Ids;
import org.springframework.stereotype.Service;

import java.util.Currency;
import java.util.Optional;

@Service
public class DepositAccountPaymentServiceImpl extends AbstractServiceImpl implements DepositAccountPaymentService {
    private static final String PAYMENT_EXECUTION_FAILED = "Payment execution failed due to: %s, payment id: %s";
    private static final String PAYMENT_CANCELLATION_FAILED = "Can`t cancel payment id:%s, as it is already executed";

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final PaymentExecutionService executionService;
    private final DepositAccountService accountService;

    public DepositAccountPaymentServiceImpl(DepositAccountConfigService depositAccountConfigService,
                                            LedgerService ledgerService, PaymentRepository paymentRepository,
                                            PaymentMapper paymentMapper, PaymentExecutionService executionService, DepositAccountService accountService) {
        super(depositAccountConfigService, ledgerService);
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
        this.executionService = executionService;
        this.accountService = accountService;
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
    public PaymentBO initiatePayment(PaymentBO payment, TransactionStatusBO status) throws PaymentWithIdExistsException, DepositAccountNotFoundException {
    	if(paymentRepository.existsById(payment.getPaymentId())) {
    		throw new PaymentWithIdExistsException(payment.getPaymentId());
    	}
    	
        Payment persistedPayment = paymentMapper.toPayment(payment);
        persistedPayment.getTargets().forEach(t -> {
        	t.setPayment(persistedPayment);
        	t.setPaymentId(Ids.id());
        });
        persistedPayment.setTransactionStatus(TransactionStatus.valueOf(status.name()));

        AmountBO amountToVerify = calculateTotalPaymentAmount(payment);

        boolean confirmationOfFunds = accountService.confirmationOfFunds(new FundsConfirmationRequestBO(null, payment.getDebtorAccount(), amountToVerify, null, null));
        if (confirmationOfFunds) {
            Payment savedPayment = paymentRepository.save(persistedPayment);
            return paymentMapper.toPaymentBO(savedPayment);
        } else {
            persistedPayment.setTransactionStatus(TransactionStatus.RJCT);
           throw new DepositAccountInsufficientFundsException(persistedPayment.getPaymentId());
        }
    }

    private AmountBO calculateTotalPaymentAmount(PaymentBO payment) {
        return payment.getTargets().stream()
                       .map(PaymentTargetBO::getInstructedAmount)
                       .reduce((left, right) -> new AmountBO(Currency.getInstance("EUR"), left.getAmount().add(right.getAmount())))
                       .orElseThrow(() -> new RuntimeException("Could not calculate total amount for payment"));
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
    public TransactionStatusBO updatePaymentStatus(String paymentId, TransactionStatusBO status) throws PaymentNotFoundException {
        Payment payment = paymentRepository.findByPaymentId(paymentId)
                                  .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        payment.setTransactionStatus(TransactionStatus.valueOf(status.name()));
        Payment p = updatePaymentStatus(payment, TransactionStatus.valueOf(status.name()));
        return TransactionStatusBO.valueOf(p.getTransactionStatus().name());
    }

    private Payment updatePaymentStatus(Payment payment, TransactionStatus updatedStatus) {
        payment.setTransactionStatus(updatedStatus);
        return paymentRepository.save(payment);
    }
}
