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
import de.adorsys.ledgers.deposit.api.exception.DepositAccountInsufficientFundsException;
import de.adorsys.ledgers.deposit.api.exception.DepositAccountNotFoundException;
import de.adorsys.ledgers.deposit.api.exception.PaymentNotFoundException;
import de.adorsys.ledgers.deposit.api.exception.PaymentWithIdExistsException;
import de.adorsys.ledgers.deposit.api.service.DepositAccountPaymentService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentCoreDataTO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentProductTO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTypeTO;
import de.adorsys.ledgers.middleware.api.domain.payment.TransactionStatusTO;
import de.adorsys.ledgers.middleware.api.domain.sca.SCAPaymentResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaStatusTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.TokenUsageTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.exception.*;
import de.adorsys.ledgers.middleware.api.service.MiddlewarePaymentService;
import de.adorsys.ledgers.middleware.impl.converter.BearerTokenMapper;
import de.adorsys.ledgers.middleware.impl.converter.PaymentConverter;
import de.adorsys.ledgers.middleware.impl.converter.ScaInfoMapper;
import de.adorsys.ledgers.middleware.impl.policies.PaymentCancelPolicy;
import de.adorsys.ledgers.middleware.impl.policies.PaymentCoreDataPolicy;
import de.adorsys.ledgers.sca.domain.AuthCodeDataBO;
import de.adorsys.ledgers.sca.domain.OpTypeBO;
import de.adorsys.ledgers.sca.domain.SCAOperationBO;
import de.adorsys.ledgers.sca.domain.ScaStatusBO;
import de.adorsys.ledgers.sca.exception.*;
import de.adorsys.ledgers.sca.service.SCAOperationService;
import de.adorsys.ledgers.um.api.domain.AisAccountAccessInfoBO;
import de.adorsys.ledgers.um.api.domain.AisConsentBO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.service.UserService;
import de.adorsys.ledgers.util.Ids;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MiddlewarePaymentServiceImpl implements MiddlewarePaymentService {
    private static final String PAYMENT_WITH_ID_S_NOT_FOUND = "Payment with id %s, not found";

    private final DepositAccountPaymentService paymentService;
    private final SCAOperationService scaOperationService;
    private final DepositAccountService accountService;
    private final PaymentConverter paymentConverter;
    private final BearerTokenMapper bearerTokenMapper;
    private final UserService userService;
    private final SCAUtils scaUtils;
    private final PaymentCancelPolicy cancelPolicy;
    private final PaymentCoreDataPolicy coreDataPolicy;
    private final AccessService accessService;
    private final ScaInfoMapper scaInfoMapper;
    private int defaultLoginTokenExpireInSeconds = 600; // 600 seconds.

    @Value("${sca.multilevel.enabled:false}")
    private boolean multilevelScaEnable;

    @Override
    public TransactionStatusTO getPaymentStatusById(String paymentId) throws PaymentNotFoundMiddlewareException {
        try {
            TransactionStatusBO paymentStatus = paymentService.getPaymentStatusById(paymentId);
            return TransactionStatusTO.valueOf(paymentStatus.name());
        } catch (PaymentNotFoundException e) {
            log.error("Payment with id: {} not found", paymentId);
            throw new PaymentNotFoundMiddlewareException(e.getMessage(), e);
        }
    }

    @Override
    public SCAPaymentResponseTO initiatePaymentCancellation(ScaInfoTO scaInfoTO, String paymentId)
            throws PaymentNotFoundMiddlewareException, PaymentProcessingMiddlewareException {
        UserBO userBO = scaUtils.userBO(scaInfoTO.getUserId());
        PaymentBO paymentBO = loadPayment(paymentId);
        TransactionStatusTO originalTxStatus = TransactionStatusTO.valueOf(paymentBO.getTransactionStatus().name());
        cancelPolicy.onCancel(paymentId, originalTxStatus);

        PaymentCoreDataTO paymentKeyData = coreDataPolicy.getCancelPaymentCoreData(paymentBO);
        SCAPaymentResponseTO response = prepareSCA(scaInfoTO, userBO, paymentBO, paymentKeyData, OpTypeBO.CANCEL_PAYMENT);

        // If exempted, execute.
        if (ScaStatusTO.EXEMPTED.equals(response.getScaStatus())) {
            try {
                TransactionStatusBO cancelPayment = paymentService.cancelPayment(paymentId);
                response.setTransactionStatus(TransactionStatusTO.valueOf(cancelPayment.name()));
            } catch (PaymentNotFoundException e) {
                // SHall not happen
                throw new AccountMiddlewareUncheckedException(e.getMessage(), e);
            }
        }

        return response;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> SCAPaymentResponseTO initiatePayment(ScaInfoTO scaInfoTO, T payment, PaymentTypeTO paymentType)
            throws AccountNotFoundMiddlewareException, PaymentWithIdMiddlewareException {
        PaymentBO paymentBO = paymentConverter.toPaymentBO(payment, paymentType.getPaymentClass());
        UserBO userBO = scaUtils.userBO(scaInfoTO.getUserId());
        checkDepositAccount(paymentBO);

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
                try {
                    status = paymentService.executePayment(paymentBO.getPaymentId(), userBO.getLogin());
                    response.setTransactionStatus(TransactionStatusTO.valueOf(status.name()));
                } catch (PaymentNotFoundException e) {
                    // Shall not happen
                    throw new AccountMiddlewareUncheckedException(e.getMessage(), e);
                }
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

    private PaymentBO persist(PaymentBO paymentBO, TransactionStatusBO status) throws PaymentWithIdMiddlewareException {
        if (paymentBO.getPaymentId() == null) {
            paymentBO.setPaymentId(Ids.id());
        }
        try {
            return paymentService.initiatePayment(paymentBO, status);
        } catch (PaymentWithIdExistsException | DepositAccountNotFoundException e) {
            throw new PaymentWithIdMiddlewareException(e.getMessage(), e);
        } catch (DepositAccountInsufficientFundsException e) {
            throw new InsufficientFundsMiddlewareException(e.getCause(), e.getMessage());
        }
    }

    private void checkDepositAccount(PaymentBO paymentBO) throws AccountNotFoundMiddlewareException {
        try {
            accountService.getDepositAccountByIban(paymentBO.getDebtorAccount().getIban(), LocalDateTime.now(), false);

        } catch (DepositAccountNotFoundException e) {
            log.error(e.getMessage());
            throw new AccountNotFoundMiddlewareException(e.getMessage());
        }
    }

    @Override
    public Object getPaymentById(String paymentId) throws PaymentNotFoundMiddlewareException {
        try {
            PaymentBO paymentResult = paymentService.getPaymentById(paymentId);
            return paymentConverter.toPaymentTO(paymentResult);
        } catch (PaymentNotFoundException e) {
            log.error(String.format("Payment with id= %s, not found", paymentId));
            throw new PaymentNotFoundMiddlewareException(e.getMessage(), e);
        }
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
    public SCAPaymentResponseTO authorizePayment(ScaInfoTO scaInfoTO, String paymentId)
            throws SCAOperationNotFoundMiddlewareException, SCAOperationValidationMiddlewareException,
                           SCAOperationExpiredMiddlewareException, SCAOperationUsedOrStolenMiddlewareException,
                           PaymentNotFoundMiddlewareException {

        PaymentBO payment = loadPayment(paymentId);
        PaymentCoreDataTO paymentKeyData = coreDataPolicy.getPaymentCoreData(payment);
        TransactionStatusBO tx = payment.getTransactionStatus();
        try {
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
        } catch (PaymentNotFoundException e) {
            log.error(e.getMessage());
            throw new AccountMiddlewareUncheckedException(e.getMessage(), e);
        }
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
            return bearerTokenMapper.toBearerTokenTO(userService.consentToken(scaInfoMapper.toScaInfoBO(scaInfoTO), aisConsent));
    }

    @Override
    public SCAPaymentResponseTO loadSCAForPaymentData(ScaInfoTO scaInfoTO, String paymentId)
            throws PaymentNotFoundMiddlewareException, SCAOperationExpiredMiddlewareException {
        SCAOperationBO a;
        try {
            a = scaOperationService.loadAuthCode(scaInfoTO.getAuthorisationId());
        } catch (SCAOperationNotFoundException e) {
            throw new SCAOperationExpiredMiddlewareException(e.getMessage(), e);
        }
        PaymentBO payment = loadPayment(paymentId);
        PaymentCoreDataTO paymentKeyData = coreDataPolicy.getPaymentCoreData(payment);
        UserTO user = scaUtils.user(scaInfoTO.getUserId());
        BearerTokenTO bearerToken = paymentAccountAccessToken(scaInfoTO, payment, user.getLogin());
        return toScaPaymentResponse(user, paymentId, payment.getTransactionStatus(), paymentKeyData, a, bearerToken);
    }

    @Override
    public SCAPaymentResponseTO selectSCAMethodForPayment(ScaInfoTO scaInfoTO, String paymentId)
            throws PaymentNotFoundMiddlewareException, SCAMethodNotSupportedMiddleException,
                           SCAOperationValidationMiddlewareException,                           SCAOperationNotFoundMiddlewareException {

        UserBO userBO = scaUtils.userBO(scaInfoTO.getUserId());
        UserTO userTO = scaUtils.user(userBO);
        PaymentBO payment = loadPayment(paymentId);
        try {
            int scaWeight = accessService.resolveScaWeightByDebtorAccount(userBO.getAccountAccesses(), payment.getDebtorAccount().getIban());

            PaymentCoreDataTO paymentKeyData = coreDataPolicy.getPaymentCoreData(payment);
            String opData = paymentKeyData.template();
            String userMessage = opData;
            AuthCodeDataBO authCodeDataBO = new AuthCodeDataBO(userBO.getLogin(), scaInfoTO.getScaMethodId(), paymentId, opData, userMessage,
                    defaultLoginTokenExpireInSeconds, OpTypeBO.PAYMENT, scaInfoTO.getAuthorisationId(), scaWeight);

            SCAOperationBO scaOperationBO = scaOperationService.generateAuthCode(authCodeDataBO, userBO, ScaStatusBO.SCAMETHODSELECTED);
            BearerTokenTO bearerToken = paymentAccountAccessToken(scaInfoTO, payment, userTO.getLogin());
            return toScaPaymentResponse(userTO, paymentId, payment.getTransactionStatus(), paymentKeyData, scaOperationBO, bearerToken);
        } catch (SCAMethodNotSupportedException e) {
            log.error(e.getMessage());
            throw new SCAMethodNotSupportedMiddleException(e);
        } catch (SCAOperationValidationException e) {
            throw new SCAOperationValidationMiddlewareException(e.getMessage(), e);
        } catch (SCAOperationNotFoundException e) {
            throw new SCAOperationNotFoundMiddlewareException(e.getMessage(), e);
        }
    }

    @Override
    public SCAPaymentResponseTO loadSCAForCancelPaymentData(ScaInfoTO scaInfoTO, String paymentId, String cancellationId)
            throws PaymentNotFoundMiddlewareException, SCAOperationExpiredMiddlewareException {
        SCAOperationBO a;
        try {
            a = scaOperationService.loadAuthCode(cancellationId);
        } catch (SCAOperationNotFoundException e) {
            throw new SCAOperationExpiredMiddlewareException(e.getMessage(), e);
        }
        UserTO user = scaUtils.user(scaInfoTO.getUserId());
        PaymentBO payment = loadPayment(paymentId);
        PaymentCoreDataTO paymentKeyData = coreDataPolicy.getCancelPaymentCoreData(payment);
        BearerTokenTO bearerToken = paymentAccountAccessToken(scaInfoTO, payment, user.getLogin());
        return toScaPaymentResponse(user, paymentId, payment.getTransactionStatus(), paymentKeyData, a, bearerToken);
    }

    @Override
    public SCAPaymentResponseTO selectSCAMethodForCancelPayment(ScaInfoTO scaInfoTO, String paymentId, String cancellationId)
            throws PaymentNotFoundMiddlewareException, SCAMethodNotSupportedMiddleException,
                           SCAOperationValidationMiddlewareException,                           SCAOperationNotFoundMiddlewareException {
        try {
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
        } catch (SCAMethodNotSupportedException e) {
            log.error(e.getMessage());
            throw new SCAMethodNotSupportedMiddleException(e);
        } catch (SCAOperationValidationException e) {
            throw new SCAOperationValidationMiddlewareException(e.getMessage(), e);
        } catch (SCAOperationNotFoundException e) {
            throw new SCAOperationNotFoundMiddlewareException(e.getMessage(), e);
        }
    }

    @Override
    public SCAPaymentResponseTO authorizeCancelPayment(ScaInfoTO scaInfoTO, String paymentId, String cancellationId)
            throws SCAOperationNotFoundMiddlewareException, SCAOperationValidationMiddlewareException,
                           SCAOperationExpiredMiddlewareException, SCAOperationUsedOrStolenMiddlewareException, PaymentNotFoundMiddlewareException {
        PaymentBO payment = loadPayment(paymentId);
        PaymentCoreDataTO paymentKeyData = coreDataPolicy.getCancelPaymentCoreData(payment);
        try {
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
        } catch (PaymentNotFoundException e) {
            log.error(e.getMessage());
            throw new AccountMiddlewareUncheckedException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("PMD.CyclomaticComplexity")
    private void validateAuthCode(String userId, PaymentBO payment, String authorisationId, String authCode, String template)
            throws SCAOperationNotFoundMiddlewareException, SCAOperationValidationMiddlewareException,
                           SCAOperationUsedOrStolenMiddlewareException, SCAOperationExpiredMiddlewareException {
        try {
            UserBO userBO = scaUtils.userBO(userId);
            int scaWeight = accessService.resolveScaWeightByDebtorAccount(userBO.getAccountAccesses(), payment.getDebtorAccount().getIban());
            if (!scaOperationService.validateAuthCode(authorisationId, payment.getPaymentId(), template, authCode, scaWeight)) {
                throw new SCAOperationValidationMiddlewareException("Wrong auth code");
            }
        } catch (SCAOperationNotFoundException e) {
            throw new SCAOperationNotFoundMiddlewareException(e);
        } catch (SCAOperationValidationException e) {
            throw new SCAOperationValidationMiddlewareException(e);
        } catch (SCAOperationUsedOrStolenException e) {
            throw new SCAOperationUsedOrStolenMiddlewareException(e);
        } catch (SCAOperationExpiredException e) {
            throw new SCAOperationExpiredMiddlewareException(e);
        }
    }

    private PaymentBO loadPayment(String paymentId) throws PaymentNotFoundMiddlewareException {
        try {
            return paymentService.getPaymentById(paymentId);
        } catch (PaymentNotFoundException e) {
            String message = String.format(PAYMENT_WITH_ID_S_NOT_FOUND, paymentId);
            log.error(message);
            throw new PaymentNotFoundMiddlewareException(message, e);
        }
    }

    /*
     * The SCA requirement shall be added as property of a deposit account permissionning.
     *
     * For now we will assume there is no sca requirement, when the user having access
     * to the account does not habe any sca data configured.
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
                                                      SCAOperationBO a, BearerTokenTO paymentAccountAccessToken) {
        SCAPaymentResponseTO response = new SCAPaymentResponseTO();
        response.setAuthorisationId(a.getId());
        response.setChosenScaMethod(scaUtils.getScaMethod(user, a.getScaMethodId()));
        response.setChallengeData(null);
        response.setExpiresInSeconds(a.getValiditySeconds());
        response.setPaymentId(paymentId);
        response.setPsuMessage(paymentKeyData.template());
        response.setScaMethods(user.getScaUserData());
        response.setScaStatus(ScaStatusTO.valueOf(a.getScaStatus().name()));
        response.setStatusDate(a.getStatusTime());
        response.setTransactionStatus(TransactionStatusTO.valueOf(tx.name()));
        response.setPaymentProduct(PaymentProductTO.getByValue(paymentKeyData.getPaymentProduct()).orElse(null));
        response.setBearerToken(paymentAccountAccessToken);
        return response;
    }
}
