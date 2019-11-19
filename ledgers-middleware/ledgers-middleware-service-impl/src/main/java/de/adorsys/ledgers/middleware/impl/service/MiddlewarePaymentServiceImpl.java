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

package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.deposit.api.domain.PaymentBO;
import de.adorsys.ledgers.deposit.api.domain.PaymentProductBO;
import de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO;
import de.adorsys.ledgers.deposit.api.service.DepositAccountPaymentService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentCoreDataTO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentProductTO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTypeTO;
import de.adorsys.ledgers.middleware.api.domain.payment.TransactionStatusTO;
import de.adorsys.ledgers.middleware.api.domain.sca.SCAPaymentResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaDataInfoTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaStatusTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.ScaUserDataTO;
import de.adorsys.ledgers.middleware.api.domain.um.TokenUsageTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareModuleException;
import de.adorsys.ledgers.middleware.api.service.MiddlewarePaymentService;
import de.adorsys.ledgers.middleware.api.service.ScaChallengeDataResolver;
import de.adorsys.ledgers.middleware.impl.converter.BearerTokenMapper;
import de.adorsys.ledgers.middleware.impl.converter.PainPaymentConverter;
import de.adorsys.ledgers.middleware.impl.converter.PaymentConverter;
import de.adorsys.ledgers.middleware.impl.converter.ScaInfoMapper;
import de.adorsys.ledgers.middleware.impl.policies.PaymentCancelPolicy;
import de.adorsys.ledgers.middleware.impl.policies.PaymentCoreDataPolicy;
import de.adorsys.ledgers.sca.domain.AuthCodeDataBO;
import de.adorsys.ledgers.sca.domain.OpTypeBO;
import de.adorsys.ledgers.sca.domain.SCAOperationBO;
import de.adorsys.ledgers.sca.domain.ScaStatusBO;
import de.adorsys.ledgers.sca.service.SCAOperationService;
import de.adorsys.ledgers.um.api.domain.AisAccountAccessInfoBO;
import de.adorsys.ledgers.um.api.domain.AisConsentBO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.service.AuthorizationService;
import de.adorsys.ledgers.util.Ids;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static de.adorsys.ledgers.middleware.api.exception.MiddlewareErrorCode.AUTHENTICATION_FAILURE;
import static de.adorsys.ledgers.middleware.api.exception.MiddlewareErrorCode.REQUEST_VALIDATION_FAILURE;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MiddlewarePaymentServiceImpl implements MiddlewarePaymentService {
    private final DepositAccountPaymentService paymentService;
    private final SCAOperationService scaOperationService;
    private final DepositAccountService accountService;
    private final PaymentConverter paymentConverter;
    private final BearerTokenMapper bearerTokenMapper;
    private final SCAUtils scaUtils;
    private final PaymentCancelPolicy cancelPolicy;
    private final PaymentCoreDataPolicy coreDataPolicy;
    private final AccessService accessService;
    private final ScaInfoMapper scaInfoMapper;
    private final AuthorizationService authorizationService;
    private final ScaChallengeDataResolver scaChallengeDataResolver;
    private final PainPaymentConverter painPaymentConverter;
    private int defaultLoginTokenExpireInSeconds = 600; // 600 seconds.

    @Value("${sca.multilevel.enabled:false}")
    private boolean multilevelScaEnable;

    @Override
    public TransactionStatusTO getPaymentStatusById(String paymentId) {
        TransactionStatusBO paymentStatus = paymentService.getPaymentStatusById(paymentId);
        return TransactionStatusTO.valueOf(paymentStatus.name());
    }

    @Override
    public SCAPaymentResponseTO initiatePaymentCancellation(ScaInfoTO scaInfoTO, String paymentId) {
        UserBO userBO = scaUtils.userBO(scaInfoTO.getUserId());
        PaymentBO paymentBO = loadPayment(paymentId);
        TransactionStatusTO originalTxStatus = TransactionStatusTO.valueOf(paymentBO.getTransactionStatus().name());
        cancelPolicy.onCancel(paymentId, originalTxStatus);

        PaymentCoreDataTO paymentKeyData = coreDataPolicy.getCancelPaymentCoreData(paymentBO);
        SCAPaymentResponseTO response = prepareSCA(scaInfoTO, userBO, paymentBO, paymentKeyData, OpTypeBO.CANCEL_PAYMENT);

        // If exempted, execute.
        if (ScaStatusTO.EXEMPTED.equals(response.getScaStatus())) {
            TransactionStatusBO cancelPayment = paymentService.cancelPayment(paymentId);
            response.setTransactionStatus(TransactionStatusTO.valueOf(cancelPayment.name()));
        }
        return response;
    }

    @Override
    public String initiatePainPayment(ScaInfoTO scaInfoTO, String payment, PaymentTypeTO paymentType) {
        return painPaymentConverter.toPayload(buildScaPaymentResponseTO(scaInfoTO, painPaymentConverter.toPaymentBO(payment, paymentType)));
    }

    @Override
    @SuppressWarnings("unchecked")
    public SCAPaymentResponseTO initiatePayment(ScaInfoTO scaInfoTO, Object payment, PaymentTypeTO paymentType) {
        return buildScaPaymentResponseTO(scaInfoTO, paymentConverter.toPaymentBO(payment, paymentType.getPaymentClass()));
    }

    private SCAPaymentResponseTO buildScaPaymentResponseTO(ScaInfoTO scaInfoTO, PaymentBO paymentBO) { //NOPMD
        if (!paymentBO.isValidAmount()) {
            throw MiddlewareModuleException.builder()
                          .devMsg("Payment validation failed! Instructed amount is invalid.")
                          .errorCode(REQUEST_VALIDATION_FAILURE)
                          .build();
        }
        UserBO userBO = scaUtils.userBO(scaInfoTO.getUserId());
        accountService.getDepositAccountByIbanAndCheckStatus(paymentBO.getDebtorAccount().getIban(), LocalDateTime.now(), false);

        TransactionStatusBO status = scaUtils.hasSCA(userBO)
                                             ? TransactionStatusBO.ACCP
                                             : TransactionStatusBO.ACTC;

        paymentBO = persist(paymentBO, status);
        status = paymentBO.getTransactionStatus();
        SCAPaymentResponseTO response = new SCAPaymentResponseTO();
        response.setMultilevelScaRequired(multilevelScaEnable);

        if (TransactionStatusBO.RJCT.equals(status)) {
            response.setScaStatus(ScaStatusTO.FAILED);
            response.setTransactionStatus(TransactionStatusTO.valueOf(status.name()));
            response.setPaymentId(paymentBO.getPaymentId());
            setPaymentProductAndType(paymentBO, response);
        } else {
            PaymentCoreDataTO paymentKeyData = coreDataPolicy.getPaymentCoreData(paymentBO);
            response = prepareSCA(scaInfoTO, userBO, paymentBO, paymentKeyData, OpTypeBO.PAYMENT);
            if (ScaStatusTO.EXEMPTED.equals(response.getScaStatus())) {
                status = paymentService.executePayment(paymentBO.getPaymentId(), userBO.getLogin());
                response.setTransactionStatus(TransactionStatusTO.valueOf(status.name()));
            }
        }
        return response;
    }

    private void setPaymentProductAndType(final PaymentBO paymentBO, final SCAPaymentResponseTO response) {
        response.setPaymentType(PaymentTypeTO.valueOf(paymentBO.getPaymentType().name()));
        if (paymentBO.getTargets() != null && !paymentBO.getTargets().isEmpty()) {
            PaymentProductBO paymentProduct = paymentBO.getTargets().iterator().next().getPaymentProduct();
            response.setPaymentProduct(PaymentProductTO.getByValue(paymentProduct.getValue()).orElse(null));
        }
    }

    private PaymentBO persist(PaymentBO paymentBO, TransactionStatusBO status) {
        if (paymentBO.getPaymentId() == null) {
            paymentBO.setPaymentId(Ids.id());
        }
        return paymentService.initiatePayment(paymentBO, status);
    }

    @Override
    public Object getPaymentById(String paymentId) {
        PaymentBO paymentResult = paymentService.getPaymentById(paymentId);
        return paymentConverter.toPaymentTO(paymentResult);
    }

    @Override
    public String iban(String paymentId) {
        return paymentService.readIbanByPaymentId(paymentId);
    }

    /*
     * Authorizes a payment. Schedule the payment for execution if no further authorization is required.
     *
     */
    @Override
    public SCAPaymentResponseTO authorizePayment(ScaInfoTO scaInfoTO, String paymentId) {
        PaymentBO payment = loadPayment(paymentId);
        PaymentCoreDataTO paymentKeyData = coreDataPolicy.getPaymentCoreData(payment);
        TransactionStatusBO tx = payment.getTransactionStatus();
        validateAuthCode(scaInfoTO.getUserId(), payment, scaInfoTO.getAuthorisationId(), scaInfoTO.getAuthCode(), paymentKeyData.template());
        boolean authCompleted = scaOperationService.authenticationCompleted(paymentId, OpTypeBO.PAYMENT);
        UserTO user = scaUtils.user(scaInfoTO.getUserId());
        if (authCompleted) {
            tx = paymentService.updatePaymentStatus(paymentId, TransactionStatusBO.ACTC);
            tx = paymentService.executePayment(paymentId, user.getLogin());
        } else if (multilevelScaEnable) {
            tx = paymentService.updatePaymentStatus(paymentId, TransactionStatusBO.PATC);
        }
        BearerTokenTO bearerToken = paymentAccountAccessToken(scaInfoTO, payment, user.getLogin());
        return toScaPaymentResponse(scaUtils.user(scaInfoTO.getUserId()), paymentId, tx, paymentKeyData, scaUtils.loadAuthCode(scaInfoTO.getAuthorisationId()), bearerToken);
    }

    private BearerTokenTO paymentAccountAccessToken(ScaInfoTO scaInfoTO, PaymentBO payment, String userName) {
        String iban = payment.getDebtorAccount().getIban();
        // Returned token can be used to access status.
        AisConsentBO aisConsent = new AisConsentBO();
        AisAccountAccessInfoBO access = new AisAccountAccessInfoBO();
        aisConsent.setAccess(access);
        List<String> asList = Collections.singletonList(iban);
        access.setAccounts(asList);
        access.setTransactions(asList);
        access.setBalances(asList);
        aisConsent.setFrequencyPerDay(0);
        aisConsent.setRecurringIndicator(true);
        // This is the user login for psd2 and not the technical id.
        aisConsent.setUserId(userName);
        return bearerTokenMapper.toBearerTokenTO(authorizationService.consentToken(scaInfoMapper.toScaInfoBO(scaInfoTO), aisConsent));
    }

    @Override
    public SCAPaymentResponseTO loadSCAForPaymentData(ScaInfoTO scaInfoTO, String paymentId) {
        SCAOperationBO scaOperation = scaOperationService.loadAuthCode(scaInfoTO.getAuthorisationId());
        PaymentBO payment = loadPayment(paymentId);
        PaymentCoreDataTO paymentKeyData = coreDataPolicy.getPaymentCoreData(payment);
        UserTO user = scaUtils.user(scaInfoTO.getUserId());
        BearerTokenTO bearerToken = paymentAccountAccessToken(scaInfoTO, payment, user.getLogin());
        return toScaPaymentResponse(user, paymentId, payment.getTransactionStatus(), paymentKeyData, scaOperation, bearerToken);
    }

    @Override
    public SCAPaymentResponseTO selectSCAMethodForPayment(ScaInfoTO scaInfoTO, String paymentId) {
        UserBO userBO = scaUtils.userBO(scaInfoTO.getUserId());
        UserTO userTO = scaUtils.user(userBO);
        PaymentBO payment = loadPayment(paymentId);
        int scaWeight = accessService.resolveScaWeightByDebtorAccount(userBO.getAccountAccesses(), payment.getDebtorAccount().getIban());

        PaymentCoreDataTO paymentKeyData = coreDataPolicy.getPaymentCoreData(payment);
        String opData = paymentKeyData.template();
        String userMessage = opData;
        AuthCodeDataBO authCodeDataBO = new AuthCodeDataBO(userBO.getLogin(), scaInfoTO.getScaMethodId(), paymentId, opData, userMessage,
                defaultLoginTokenExpireInSeconds, OpTypeBO.PAYMENT, scaInfoTO.getAuthorisationId(), scaWeight);

        SCAOperationBO scaOperationBO = scaOperationService.generateAuthCode(authCodeDataBO, userBO, ScaStatusBO.SCAMETHODSELECTED);
        BearerTokenTO bearerToken = paymentAccountAccessToken(scaInfoTO, payment, userTO.getLogin());
        return toScaPaymentResponse(userTO, paymentId, payment.getTransactionStatus(), paymentKeyData, scaOperationBO, bearerToken);
    }

    @Override
    public SCAPaymentResponseTO loadSCAForCancelPaymentData(ScaInfoTO scaInfoTO, String paymentId, String cancellationId) {
        SCAOperationBO scaOperation = scaOperationService.loadAuthCode(cancellationId);
        UserTO user = scaUtils.user(scaInfoTO.getUserId());
        PaymentBO payment = loadPayment(paymentId);
        PaymentCoreDataTO paymentKeyData = coreDataPolicy.getCancelPaymentCoreData(payment);
        BearerTokenTO bearerToken = paymentAccountAccessToken(scaInfoTO, payment, user.getLogin());
        return toScaPaymentResponse(user, paymentId, payment.getTransactionStatus(), paymentKeyData, scaOperation, bearerToken);
    }

    @Override
    public SCAPaymentResponseTO selectSCAMethodForCancelPayment(ScaInfoTO scaInfoTO, String paymentId, String cancellationId) {
        UserBO userBO = scaUtils.userBO(scaInfoTO.getUserId());
        UserTO userTO = scaUtils.user(userBO);
        PaymentBO payment = loadPayment(paymentId);
        int scaWeight = accessService.resolveScaWeightByDebtorAccount(userBO.getAccountAccesses(), payment.getDebtorAccount().getIban());

        PaymentCoreDataTO paymentKeyData = coreDataPolicy.getCancelPaymentCoreData(payment);
        String template = paymentKeyData.template();
        String opData = template;
        String userMessage = template;
        AuthCodeDataBO authCodeDataBO = new AuthCodeDataBO(userBO.getLogin(), scaInfoTO.getScaMethodId(),
                paymentId, opData, userMessage,
                defaultLoginTokenExpireInSeconds,
                OpTypeBO.CANCEL_PAYMENT, cancellationId, scaWeight);

        SCAOperationBO scaOperationBO = scaOperationService.generateAuthCode(authCodeDataBO, userBO, ScaStatusBO.SCAMETHODSELECTED);
        BearerTokenTO bearerToken = paymentAccountAccessToken(scaInfoTO, payment, userTO.getLogin());
        return toScaPaymentResponse(userTO, paymentId, payment.getTransactionStatus(), paymentKeyData, scaOperationBO, bearerToken);
    }

    @Override
    public SCAPaymentResponseTO authorizeCancelPayment(ScaInfoTO scaInfoTO, String paymentId, String cancellationId) {
        PaymentBO payment = loadPayment(paymentId);
        PaymentCoreDataTO paymentKeyData = coreDataPolicy.getCancelPaymentCoreData(payment);
        validateAuthCode(scaInfoTO.getUserId(), payment, cancellationId, scaInfoTO.getAuthCode(), paymentKeyData.template());
        TransactionStatusBO tx = payment.getTransactionStatus();
        if (scaOperationService.authenticationCompleted(paymentId, OpTypeBO.CANCEL_PAYMENT)) {
            tx = paymentService.cancelPayment(paymentId);
        } else if (multilevelScaEnable) {
            tx = paymentService.updatePaymentStatus(paymentId, TransactionStatusBO.PATC);
        }
        UserTO user = scaUtils.user(scaInfoTO.getUserId());
        BearerTokenTO bearerToken = paymentAccountAccessToken(scaInfoTO, payment, user.getLogin());
        return toScaPaymentResponse(user, paymentId, tx,
                paymentKeyData, scaUtils.loadAuthCode(cancellationId), bearerToken);
    }

    private void validateAuthCode(String userId, PaymentBO payment, String authorisationId, String authCode, String template) {
        UserBO userBO = scaUtils.userBO(userId);
        int scaWeight = accessService.resolveScaWeightByDebtorAccount(userBO.getAccountAccesses(), payment.getDebtorAccount().getIban());
        if (!scaOperationService.validateAuthCode(authorisationId, payment.getPaymentId(), template, authCode, scaWeight)) {
            throw MiddlewareModuleException.builder()
                          .errorCode(AUTHENTICATION_FAILURE)
                          .devMsg("Wrong auth code")
                          .build();
        }
    }

    private PaymentBO loadPayment(String paymentId) {
        return paymentService.getPaymentById(paymentId);
    }

    /*
     * The SCA requirement shall be added as property of a deposit account permission.
     *
     * For now we will assume there is no sca requirement, when the user having access
     * to the account does not have any sca data configured.
     */
    @SuppressWarnings("PMD.UnusedFormalParameter")
    private boolean scaRequired(PaymentBO payment, UserBO user, OpTypeBO opType) {
        return scaUtils.hasSCA(user);
    }

    private SCAPaymentResponseTO prepareSCA(ScaInfoTO scaInfoTO, UserBO user, PaymentBO payment, PaymentCoreDataTO paymentKeyData, OpTypeBO opType) {
        UserTO userTo = scaUtils.user(user);
        String authorisationId = scaUtils.authorisationId(scaInfoTO);
        String paymentKeyDataTemplate = paymentKeyData.template();
        String opData = paymentKeyDataTemplate;
        String userMessage = paymentKeyDataTemplate;

        BearerTokenTO paymentAccountAccessToken = paymentAccountAccessToken(scaInfoTO, payment, userTo.getLogin());

        if (!scaRequired(payment, user, opType)) {
            SCAPaymentResponseTO response = new SCAPaymentResponseTO();
            response.setAuthorisationId(authorisationId);
            response.setPaymentId(payment.getPaymentId());
            response.setPsuMessage(paymentKeyData.exemptedTemplate());
            response.setScaStatus(ScaStatusTO.EXEMPTED);
            response.setStatusDate(LocalDateTime.now());
            response.setTransactionStatus(TransactionStatusTO.valueOf(payment.getTransactionStatus().name()));
            response.setBearerToken(paymentAccountAccessToken);
            setPaymentProductAndType(payment, response);
            return response;
        } else {
            int scaWeight = accessService.resolveScaWeightByDebtorAccount(user.getAccountAccesses(), payment.getDebtorAccount().getIban());
            AuthCodeDataBO authCodeData = new AuthCodeDataBO(user.getLogin(), null,
                    payment.getPaymentId(), opData, userMessage,
                    defaultLoginTokenExpireInSeconds, opType, authorisationId, scaWeight);
            // start SCA
            TokenUsageTO tokenUsage = scaInfoTO.getTokenUsage();
            ScaStatusBO scaStatus = ScaStatusBO.PSUIDENTIFIED;
            if (TokenUsageTO.DELEGATED_ACCESS.equals(tokenUsage)) {
                scaStatus = ScaStatusBO.PSUAUTHENTICATED;
            }
            SCAOperationBO scaOperationBO = scaOperationService.createAuthCode(authCodeData, scaStatus);
            return toScaPaymentResponse(userTo, payment.getPaymentId(), payment.getTransactionStatus(), paymentKeyData, scaOperationBO, paymentAccountAccessToken);
        }
    }

    private SCAPaymentResponseTO toScaPaymentResponse(UserTO user, String paymentId, TransactionStatusBO tx,
                                                      PaymentCoreDataTO paymentKeyData,
                                                      SCAOperationBO operation, BearerTokenTO paymentAccountAccessToken) {
        SCAPaymentResponseTO response = new SCAPaymentResponseTO();
        response.setAuthorisationId(operation.getId());
        ScaUserDataTO userData = scaUtils.getScaMethod(user, operation.getScaMethodId());
        response.setChosenScaMethod(userData);
        if (userData != null) {
            response.setChallengeData(scaChallengeDataResolver.resolveScaChallengeData(userData.getScaMethod())
                                              .getChallengeData(new ScaDataInfoTO(userData, operation.getTan())));
        }
        response.setExpiresInSeconds(operation.getValiditySeconds());
        response.setPaymentId(paymentId);
        response.setPsuMessage(paymentKeyData.template());
        response.setScaMethods(user.getScaUserData());
        response.setScaStatus(ScaStatusTO.valueOf(operation.getScaStatus().name()));
        response.setStatusDate(operation.getStatusTime());
        response.setTransactionStatus(TransactionStatusTO.valueOf(tx.name()));
        response.setPaymentProduct(PaymentProductTO.getByValue(paymentKeyData.getPaymentProduct()).orElse(null));
        response.setBearerToken(paymentAccountAccessToken);
        return response;
    }
}
