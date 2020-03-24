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
import de.adorsys.ledgers.deposit.api.service.DepositAccountConfigService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountPaymentService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.deposit.api.service.mappers.PaymentMapper;
import de.adorsys.ledgers.deposit.db.domain.AccountReference;
import de.adorsys.ledgers.deposit.db.domain.Payment;
import de.adorsys.ledgers.deposit.db.domain.PaymentType;
import de.adorsys.ledgers.deposit.db.domain.TransactionStatus;
import de.adorsys.ledgers.deposit.db.repository.PaymentRepository;
import de.adorsys.ledgers.postings.api.service.LedgerService;
import de.adorsys.ledgers.util.Ids;
import de.adorsys.ledgers.util.exception.DepositModuleException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static de.adorsys.ledgers.util.exception.DepositErrorCode.*;

@Service
public class DepositAccountPaymentServiceImpl extends AbstractServiceImpl implements DepositAccountPaymentService {
    private static final String PAYMENT_EXECUTION_FAILED = "Payment execution failed due to: %s, payment id: %s";
    private static final String PAYMENT_CANCELLATION_FAILED = "Can`t cancel payment id:%s, as it is already executed";

    @Value("${payment-products.instant: instant-sepa-credit-transfers,target-2-payments}")
    private Set<String> instantPayments = new HashSet<>();

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
    public TransactionStatusBO getPaymentStatusById(String paymentId) {
        return getPaymentById(paymentId).getTransactionStatus();
    }

    @Override
    public PaymentBO getPaymentById(String paymentId) {
        return paymentMapper.toPaymentBO(getPaymentEntityById(paymentId));
    }

    @Override
    public PaymentBO initiatePayment(PaymentBO payment, TransactionStatusBO status) {
        if (paymentRepository.existsById(payment.getPaymentId())) {
            throw DepositModuleException.builder()
                          .errorCode(PAYMENT_WITH_ID_EXISTS)
                          .devMsg(String.format("Payment with id: %s already exists!", payment.getPaymentId()))
                          .build();
        }

        Payment paymentToPersist = paymentMapper.toPayment(payment);
        paymentToPersist.getTargets().forEach(t -> {
            t.setPayment(paymentToPersist);
            t.setPaymentId(Ids.id());
        });

        AmountBO amountToVerify = executionService.calculateTotalPaymentAmount(payment);
        boolean confirmationOfFunds = accountService.confirmationOfFunds(new FundsConfirmationRequestBO(null, payment.getDebtorAccount(), amountToVerify, null, null));

        if (confirmationOfFunds) {
            paymentToPersist.setTransactionStatus(TransactionStatus.valueOf(status.name()));
            Payment savedPayment = paymentRepository.save(paymentToPersist);
            return paymentMapper.toPaymentBO(savedPayment);
        } else {
            throw DepositModuleException.builder()
                          .errorCode(INSUFFICIENT_FUNDS)
                          .devMsg(String.format("Payment with id: %s failed due to Insufficient Funds Available", paymentToPersist.getPaymentId()))
                          .build();
        }
    }

    /**
     * Execute a payment. This principally applies to: - Single payment - Future
     * date payment - Periodic Payment - Bulk Payment with batch execution
     * <p>
     * + Bulk payment without batch execution will be split into single payments and
     * each single payment will be individually sent to this method.
     */
    @Override
    public TransactionStatusBO executePayment(String paymentId, String userName) {
        return paymentRepository.findByPaymentIdAndTransactionStatus(paymentId, TransactionStatus.ACTC)
                       .map(p -> isInstantPayment(p)
                                      ? executionService.executePayment(p, userName)
                                      : executionService.schedulePayment(p))
                       .orElseThrow(() -> DepositModuleException.builder()
                                                  .errorCode(PAYMENT_PROCESSING_FAILURE)
                                                  .devMsg(String.format(PAYMENT_EXECUTION_FAILED, "Payment not found", paymentId))
                                                  .build());
    }

    @Override
    public TransactionStatusBO cancelPayment(String paymentId) {
        Payment storedPayment = getPaymentEntityById(paymentId);
        if (storedPayment.getTransactionStatus() == TransactionStatus.ACSC) {
            throw DepositModuleException.builder()
                          .errorCode(PAYMENT_PROCESSING_FAILURE)
                          .devMsg(String.format(PAYMENT_CANCELLATION_FAILED, paymentId))
                          .build();
        }
        Payment p = updatePaymentStatus(storedPayment, TransactionStatus.CANC);
        return TransactionStatusBO.valueOf(p.getTransactionStatus().name());
    }

    @Override
    public String readIbanByPaymentId(String paymentId) {
        return paymentRepository.findById(paymentId)
                       .map(Payment::getDebtorAccount)
                       .map(AccountReference::getIban)
                       .orElse(null);
    }

    @Override
    public TransactionStatusBO updatePaymentStatus(String paymentId, TransactionStatusBO status) {
        Payment payment = getPaymentEntityById(paymentId);
        payment.setTransactionStatus(TransactionStatus.valueOf(status.name()));
        Payment p = updatePaymentStatus(payment, TransactionStatus.valueOf(status.name()));
        return TransactionStatusBO.valueOf(p.getTransactionStatus().name());
    }

    @Override
    public List<PaymentBO> getPaymentsByTypeStatusAndDebtor(PaymentTypeBO paymentType, TransactionStatusBO status, List<AccountReferenceBO> referenceList) {
        List<Payment> payments = referenceList.stream()
                                         .map(r -> paymentRepository.findAllByDebtorAccount(r.getIban(), r.getCurrency().getCurrencyCode(), PaymentType.valueOf(paymentType.name()), TransactionStatus.valueOf(status.name())))
                                         .flatMap(List::stream)
                                         .collect(Collectors.toList());


        return paymentMapper.toPaymentBOList(payments);
    }

    private Payment updatePaymentStatus(Payment payment, TransactionStatus updatedStatus) {
        payment.setTransactionStatus(updatedStatus);
        return paymentRepository.save(payment);
    }

    private Payment getPaymentEntityById(String paymentId) {
        return paymentRepository.findById(paymentId)
                       .orElseThrow(() -> DepositModuleException.builder()
                                                  .errorCode(PAYMENT_NOT_FOUND)
                                                  .devMsg(String.format("Payment with id: %s not found!", paymentId))
                                                  .build());
    }

    private boolean isInstantPayment(Payment payment) {
        return instantPayments.contains(payment.getPaymentProduct());
    }
}
