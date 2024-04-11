/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.deposit.api.domain.PaymentBO;
import de.adorsys.ledgers.deposit.api.domain.PaymentTypeBO;
import de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO;
import de.adorsys.ledgers.deposit.api.service.DepositAccountPaymentService;
import de.adorsys.ledgers.middleware.api.domain.general.StepOperation;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTO;
import de.adorsys.ledgers.middleware.api.domain.payment.TransactionStatusTO;
import de.adorsys.ledgers.middleware.api.domain.sca.SCAPaymentResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaStatusTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.service.MiddlewarePaymentService;
import de.adorsys.ledgers.middleware.impl.config.PaymentValidatorService;
import de.adorsys.ledgers.middleware.impl.converter.PageMapper;
import de.adorsys.ledgers.middleware.impl.converter.PaymentConverter;
import de.adorsys.ledgers.middleware.impl.converter.ScaResponseResolver;
import de.adorsys.ledgers.middleware.impl.policies.PaymentCancelPolicy;
import de.adorsys.ledgers.middleware.impl.service.message.PsuMessageResolver;
import de.adorsys.ledgers.sca.domain.OpTypeBO;
import de.adorsys.ledgers.sca.service.SCAOperationService;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.util.domain.CustomPageImpl;
import de.adorsys.ledgers.util.domain.CustomPageableImpl;
import de.adorsys.ledgers.util.exception.ScaModuleException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO.*;
import static de.adorsys.ledgers.middleware.api.domain.Constants.SCOPE_FULL_ACCESS;
import static de.adorsys.ledgers.sca.domain.OpTypeBO.CANCEL_PAYMENT;
import static de.adorsys.ledgers.sca.domain.OpTypeBO.PAYMENT;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MiddlewarePaymentServiceImpl implements MiddlewarePaymentService {
    private final DepositAccountPaymentService paymentService;
    private final SCAOperationService scaOperationService;
    private final PaymentConverter paymentConverter;
    private final SCAUtils scaUtils;
    private final AccessService accessService;
    private final ScaResponseResolver scaResponseResolver;
    private final PageMapper pageMapper;
    private final PaymentValidatorService paymentValidator;
    private final PsuMessageResolver messageResolver;

    @Value("${ledgers.sca.multilevel.enabled:false}")
    private boolean multilevelScaEnable;

    @Override
    public TransactionStatusTO getPaymentStatusById(String paymentId) {
        TransactionStatusBO paymentStatus = paymentService.getPaymentStatusById(paymentId);
        return TransactionStatusTO.valueOf(paymentStatus.name());
    }

    @Override
    public SCAPaymentResponseTO initiatePaymentCancellation(ScaInfoTO scaInfoTO, String paymentId) {
        UserBO userBO = scaUtils.userBO(scaInfoTO.getUserLogin());
        PaymentBO payment = paymentService.getPaymentById(paymentId);
        payment.setRequestedExecutionTime(LocalTime.now().plusMinutes(10));
        PaymentCancelPolicy.onCancel(paymentId, payment.getTransactionStatus());
        return prepareScaAndResolveResponse(scaInfoTO, payment, userBO, CANCEL_PAYMENT);
    }

    @Override
    public SCAPaymentResponseTO initiatePayment(ScaInfoTO scaInfoTO, PaymentTO payment) {
        PaymentBO paymentBO = paymentConverter.toPaymentBO(payment);
        UserBO user = scaUtils.userBO(scaInfoTO.getUserLogin());
        paymentValidator.validate(paymentBO, user);
        TransactionStatusBO status = user.hasSCA()
                                             ? ACCP
                                             : ACTC;
        return prepareScaAndResolveResponse(scaInfoTO, paymentService.initiatePayment(paymentBO, status), user, PAYMENT);
    }

    @Override
    public SCAPaymentResponseTO executePayment(ScaInfoTO scaInfoTO, String paymentId) {
        PaymentBO payment = paymentService.getPaymentById(paymentId);
        TransactionStatusBO status = paymentService.updatePaymentStatus(paymentId, scaInfoTO.hasScope(SCOPE_FULL_ACCESS) ? ACTC : PATC);
        if (status == ACTC) {
            status = paymentService.executePayment(payment.getPaymentId(), scaInfoTO.getUserLogin());
        }
        return new SCAPaymentResponseTO(payment.getPaymentId(), status.name(), payment.getPaymentType().name(), payment.getPaymentProduct());
    }

    private SCAPaymentResponseTO prepareScaAndResolveResponse(ScaInfoTO scaInfoTO, PaymentBO payment, UserBO user, OpTypeBO opType) {
        boolean isScaRequired = user.hasSCA();
        String psuMessage = messageResolver.message(StepOperation.INITIATE_OPERATION_OBJECT, opType, isScaRequired, payment);
        BearerTokenTO token = accessService.exchangeTokenStartSca(isScaRequired, scaInfoTO.getAccessToken());
        ScaStatusTO scaStatus = isScaRequired
                                        ? ScaStatusTO.PSUAUTHENTICATED
                                        : ScaStatusTO.EXEMPTED;
        int scaWeight = user.resolveWeightForAccount(payment.getAccountId());
        SCAPaymentResponseTO response = new SCAPaymentResponseTO();
        scaResponseResolver.updateScaResponseFields(user, response, null, psuMessage, token, scaStatus, scaWeight);
        return scaResponseResolver.updatePaymentRelatedResponseFields(response, payment);
    }

    @Override
    public PaymentTO getPaymentById(String paymentId) {
        PaymentBO paymentResult = paymentService.getPaymentById(paymentId);
        return paymentConverter.toPaymentTO(paymentResult);
    }

    /*
     * Authorizes a payment. Schedule the payment for execution if no further authorization is required.
     *
     */

    @Override
    @Transactional(noRollbackFor = ScaModuleException.class)
    public SCAPaymentResponseTO authorizePayment(ScaInfoTO scaInfoTO, String paymentId) {
        return authorizeOperation(scaInfoTO, paymentId, PAYMENT);
    }

    @Override
    @Transactional(noRollbackFor = ScaModuleException.class)
    public SCAPaymentResponseTO authorizeCancelPayment(ScaInfoTO scaInfoTO, String paymentId) {
        return authorizeOperation(scaInfoTO, paymentId, CANCEL_PAYMENT);
    }

    @Override
    public List<PaymentTO> getPendingPeriodicPayments(ScaInfoTO scaInfoTO) {
        Set<String> accountIds = scaUtils.userBO(scaInfoTO.getUserLogin()).getAccountIds();
        List<PaymentBO> payments = paymentService.getPaymentsByTypeStatusAndDebtor(PaymentTypeBO.PERIODIC, ACSP, accountIds);
        return paymentConverter.toPaymentTOList(payments);
    }

    @Override
    public CustomPageImpl<PaymentTO> getPendingPeriodicPaymentsPaged(ScaInfoTO scaInfo, CustomPageableImpl pageable) {
        Set<String> accountIds = scaUtils.userBO(scaInfo.getUserLogin()).getAccountIds();
        return pageMapper.toCustomPageImpl(
                paymentService.getPaymentsByTypeStatusAndDebtorPaged(PaymentTypeBO.PERIODIC, ACSP, accountIds, PageRequest.of(pageable.getPage(), pageable.getSize()))
                        .map(paymentConverter::toPaymentTO));
    }

    @Override
    public CustomPageImpl<PaymentTO> getAllPaymentsPaged(ScaInfoTO scaInfo, CustomPageableImpl pageable) {
        Set<PaymentTypeBO> paymentType = Set.of(PaymentTypeBO.PERIODIC, PaymentTypeBO.SINGLE,PaymentTypeBO.BULK);
        Set<TransactionStatusBO> status = Set.of(values());
        Set<String> accountIds = scaUtils.userBO(scaInfo.getUserLogin()).getAccountIds();
        return pageMapper.toCustomPageImpl(
                paymentService.getPaymentsByTypeStatusAndDebtorInPaged(paymentType, status, accountIds, PageRequest.of(pageable.getPage(), pageable.getSize()))
                        .map(paymentConverter::toPaymentTO));
    }


    private SCAPaymentResponseTO authorizeOperation(ScaInfoTO scaInfoTO, String paymentId, OpTypeBO opType) {
        PaymentBO payment = paymentService.getPaymentById(paymentId);
        if (scaOperationService.authenticationCompleted(paymentId, opType)) {
            performExecuteOrCancelOperation(scaInfoTO, paymentId, opType, payment);
        } else if (multilevelScaEnable) {
            payment.setTransactionStatus(paymentService.updatePaymentStatus(paymentId, PATC));
        }
        UserBO user = scaUtils.userBO(scaInfoTO.getUserLogin());
        SCAPaymentResponseTO response = new SCAPaymentResponseTO();
        int scaWeight = user.resolveWeightForAccount(payment.getAccountId());
        String psuMessage = messageResolver.message(StepOperation.AUTHORIZE, opType, true, payment);
        scaResponseResolver.updateScaResponseFields(user, new SCAPaymentResponseTO(), null, psuMessage,
                                                    null, ScaStatusTO.FINALISED, scaWeight);
        return scaResponseResolver.updatePaymentRelatedResponseFields(response, payment);
    }

    private void performExecuteOrCancelOperation(ScaInfoTO scaInfoTO, String paymentId, OpTypeBO opType, PaymentBO payment) {
        if (opType == PAYMENT) {
            paymentService.updatePaymentStatus(paymentId, ACTC);
            payment.setTransactionStatus(paymentService.executePayment(paymentId, scaInfoTO.getUserLogin()));
        } else {
            payment.setTransactionStatus(paymentService.cancelPayment(paymentId));
        }
    }
}
