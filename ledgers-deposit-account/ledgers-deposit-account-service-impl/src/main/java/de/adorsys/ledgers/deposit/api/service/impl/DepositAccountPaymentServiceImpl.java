/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
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
import de.adorsys.ledgers.deposit.db.repository.PaymentTargetRepository;
import de.adorsys.ledgers.postings.api.service.LedgerService;
import de.adorsys.ledgers.util.exception.DepositModuleException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Value("${ledgers.payment-products.instant: instant-sepa-credit-transfers,target-2-payments}")
    private final Set<String> instantPayments = new HashSet<>();

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final PaymentExecutionService executionService;
    private final DepositAccountService accountService;
    private final PaymentTargetRepository targetRepository;

    public DepositAccountPaymentServiceImpl(DepositAccountConfigService depositAccountConfigService,
                                            LedgerService ledgerService, PaymentRepository paymentRepository,
                                            PaymentMapper paymentMapper, PaymentExecutionService executionService, DepositAccountService accountService, PaymentTargetRepository targetRepository) {
        super(depositAccountConfigService, ledgerService);
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
        this.executionService = executionService;
        this.accountService = accountService;
        this.targetRepository = targetRepository;
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
        paymentToPersist.getTargets().forEach(t -> t.setPayment(paymentToPersist));

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
    public List<PaymentBO> getPaymentsByTypeStatusAndDebtor(PaymentTypeBO paymentType, TransactionStatusBO status, Set<String> accountIds) {
        List<Payment> payments = paymentRepository.findAllByAccountIdInAndPaymentTypeAndTransactionStatus(accountIds, PaymentType.valueOf(paymentType.name()), TransactionStatus.valueOf(status.name()));
        return paymentMapper.toPaymentBOList(payments);
    }

    @Override
    public Page<PaymentBO> getPaymentsByTypeStatusAndDebtorPaged(PaymentTypeBO paymentType, TransactionStatusBO status, Set<String> accountIds, Pageable pageable) {
        return paymentRepository.findAllByAccountIdInAndPaymentTypeAndTransactionStatus(accountIds, PaymentType.valueOf(paymentType.name()), TransactionStatus.valueOf(status.name()), pageable)
                .map(paymentMapper::toPaymentBO);
    }

    @Override
    public Page<PaymentBO> getPaymentsByTypeStatusAndDebtorInPaged(Set<PaymentTypeBO> paymentType, Set<TransactionStatusBO> status, Set<String> accountIds, Pageable pageable) {
        Set<PaymentType> types = paymentType.stream().map(t -> PaymentType.valueOf(t.name())).collect(Collectors.toSet());
        Set<TransactionStatus> statuses = status.stream().map(s -> TransactionStatus.valueOf(s.name())).collect(Collectors.toSet());
        return paymentRepository.findAllByAccountIdInAndPaymentTypeInAndTransactionStatusInOrderByUpdatedDesc(accountIds, types, statuses, pageable)
                .map(paymentMapper::toPaymentBO);
    }

    @Override
    public boolean existingTargetById(String paymentTargetId) {
        return targetRepository.existsById(paymentTargetId);
    }

    @Override
    public boolean existingPaymentById(String paymentId) {
        return paymentRepository.existsById(paymentId);
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
