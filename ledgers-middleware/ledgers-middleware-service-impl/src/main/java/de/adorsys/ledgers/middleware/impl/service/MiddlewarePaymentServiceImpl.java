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

import de.adorsys.ledgers.deposit.api.domain.AccountReferenceBO;
import de.adorsys.ledgers.deposit.api.domain.DepositAccountBO;
import de.adorsys.ledgers.deposit.api.domain.PaymentBO;
import de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO;
import de.adorsys.ledgers.deposit.api.service.DepositAccountPaymentService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentCoreDataTO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTypeTO;
import de.adorsys.ledgers.middleware.api.domain.payment.TransactionStatusTO;
import de.adorsys.ledgers.middleware.api.domain.sca.SCAPaymentResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaStatusTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareErrorCode;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareModuleException;
import de.adorsys.ledgers.middleware.api.service.MiddlewarePaymentService;
import de.adorsys.ledgers.middleware.impl.converter.*;
import de.adorsys.ledgers.middleware.impl.policies.PaymentCancelPolicy;
import de.adorsys.ledgers.middleware.impl.policies.PaymentCoreDataPolicy;
import de.adorsys.ledgers.sca.domain.OpTypeBO;
import de.adorsys.ledgers.sca.domain.SCAOperationBO;
import de.adorsys.ledgers.sca.service.SCAOperationService;
import de.adorsys.ledgers.um.api.domain.AisAccountAccessInfoBO;
import de.adorsys.ledgers.um.api.domain.AisConsentBO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.service.AuthorizationService;
import de.adorsys.ledgers.util.Ids;
import de.adorsys.ledgers.util.exception.DepositModuleException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO.*;
import static de.adorsys.ledgers.middleware.api.domain.sca.ScaStatusTO.FINALISED;
import static de.adorsys.ledgers.middleware.api.domain.sca.ScaStatusTO.SCAMETHODSELECTED;
import static de.adorsys.ledgers.middleware.api.exception.MiddlewareErrorCode.*;
import static de.adorsys.ledgers.sca.domain.OpTypeBO.CANCEL_PAYMENT;
import static de.adorsys.ledgers.sca.domain.OpTypeBO.PAYMENT;

@Slf4j
@Service
@Transactional
@SuppressWarnings({"PMD.TooManyStaticImports", "PMD.TooManyMethods"})
@RequiredArgsConstructor
public class MiddlewarePaymentServiceImpl implements MiddlewarePaymentService {
    private final DepositAccountPaymentService paymentService;
    private final SCAOperationService scaOperationService;
    private final DepositAccountService accountService;
    private final PaymentConverter paymentConverter;
    private final BearerTokenMapper bearerTokenMapper;
    private final SCAUtils scaUtils;
    private final PaymentCoreDataPolicy coreDataPolicy;
    private final AccessService accessService;
    private final ScaInfoMapper scaInfoMapper;
    private final AuthorizationService authorizationService;
    private final PainPaymentConverter painPaymentConverter;
    private final ScaResponseResolver scaResponseResolver;

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
        paymentBO.setRequestedExecutionTime(LocalTime.now().plusMinutes(10));
        PaymentCancelPolicy.onCancel(paymentId, paymentBO.getTransactionStatus());
        return prepareScaAndResolveResponse(scaInfoTO, paymentBO, userBO, CANCEL_PAYMENT);
    }

    @Override
    public String initiatePainPayment(ScaInfoTO scaInfoTO, String payment, PaymentTypeTO paymentType) {
        return painPaymentConverter.toPayload(checkPaymentAndPrepareResponse(scaInfoTO, painPaymentConverter.toPaymentBO(payment, paymentType)));
    }

    @Override
    @SuppressWarnings("unchecked")
    public SCAPaymentResponseTO initiatePayment(ScaInfoTO scaInfoTO, Object payment, PaymentTypeTO paymentType) {
        return checkPaymentAndPrepareResponse(scaInfoTO, paymentConverter.toPaymentBO(payment, paymentType.getPaymentClass()));
    }

    @SuppressWarnings("PMD.PrematureDeclaration")
    private SCAPaymentResponseTO checkPaymentAndPrepareResponse(ScaInfoTO scaInfoTO, PaymentBO paymentBO) {
        if (!paymentBO.isValidAmount()) {
            throw MiddlewareModuleException.builder()
                          .devMsg("Payment validation failed! Instructed amount is invalid.")
                          .errorCode(REQUEST_VALIDATION_FAILURE)
                          .build();
        }
        DepositAccountBO debtorAccount = checkAccountStatusAndCurrencyMatch(paymentBO.getDebtorAccount());
        try {
            paymentBO.getTargets()
                    .forEach(t -> {
                        DepositAccountBO acc = checkAccountStatusAndCurrencyMatch(t.getCreditorAccount());
                        t.setCreditorAccount(acc.getReference());
                    });
        } catch (MiddlewareModuleException e) {
            if (EnumSet.of(ACCOUNT_DISABLED, CURRENCY_MISMATCH).contains(e.getErrorCode())) {
                log.error(e.getDevMsg());
                throw e;
            }
        } catch (DepositModuleException e) {
            log.info("Creditor account is located in another ASPSP");
        }
        paymentBO.getDebtorAccount().setCurrency(debtorAccount.getCurrency());

        UserBO user = scaUtils.userBO(scaInfoTO.getUserId());
        TransactionStatusBO status = scaUtils.hasSCA(user)
                                             ? ACCP
                                             : ACTC;
        return prepareScaAndResolveResponse(scaInfoTO, persist(paymentBO, status), user, PAYMENT);
    }

    private SCAPaymentResponseTO prepareScaAndResolveResponse(ScaInfoTO scaInfoTO, PaymentBO payment, UserBO user, OpTypeBO opType) {
        boolean isScaRequired = scaRequired(user);
        SCAPaymentResponseTO response = new SCAPaymentResponseTO();
        String authorisationId = scaUtils.authorisationId(scaInfoTO);
        String psuMessage = resolvePsuMessage(isScaRequired, payment, opType);
        BearerTokenTO token = paymentAccountAccessToken(scaInfoTO, payment.getDebtorAccount().getIban());
        ScaStatusTO scaStatus = scaResponseResolver.resolveScaStatus(scaInfoTO.getTokenUsage(), isScaRequired);
        int scaWeight = accessService.resolveScaWeightByDebtorAccount(user.getAccountAccesses(), payment.getDebtorAccount().getIban());
        scaResponseResolver.updateScaResponseFields(user, response, authorisationId, psuMessage, token, scaStatus, scaWeight);

        if (!isScaRequired) {
            executeExemptedInitiationOperation(payment, user, opType);
        } else {
            scaResponseResolver.prepareScaAndUpdateResponse(payment.getPaymentId(), response, authorisationId, psuMessage, scaWeight, user, opType);
        }
        return scaResponseResolver.updatePaymentRelatedResponseFields(response, payment);
    }

    private void executeExemptedInitiationOperation(PaymentBO payment, UserBO user, OpTypeBO opType) {
        TransactionStatusBO transactionStatus = payment.getTransactionStatus();
        if (opType == PAYMENT) {
            transactionStatus = paymentService.executePayment(payment.getPaymentId(), user.getLogin());
        } else if (opType == CANCEL_PAYMENT) {
            transactionStatus = paymentService.cancelPayment(payment.getPaymentId());
        }
        payment.setTransactionStatus(transactionStatus);
    }

    private String resolvePsuMessage(boolean isScaRequired, PaymentBO payment, OpTypeBO opType) {
        PaymentCoreDataTO paymentKeyData = coreDataPolicy.getPaymentCoreData(opType, payment);
        return isScaRequired
                       ? paymentKeyData.template()
                       : paymentKeyData.exemptedTemplate();
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
        return authorizeOperation(scaInfoTO, paymentId, null, PAYMENT);
    }

    @Override
    public SCAPaymentResponseTO authorizeCancelPayment(ScaInfoTO scaInfoTO, String paymentId, String cancellationId) {
        return authorizeOperation(scaInfoTO, paymentId, cancellationId, CANCEL_PAYMENT);
    }

    private SCAPaymentResponseTO authorizeOperation(ScaInfoTO scaInfoTO, String paymentId, String cancellationId, OpTypeBO opType) {
        PaymentBO payment = loadPayment(paymentId);
        PaymentCoreDataTO paymentKeyData = coreDataPolicy.getPaymentCoreData(opType, payment);

        String authorisationId = opType == PAYMENT ? scaInfoTO.getAuthorisationId() : cancellationId;
        validateAuthCode(scaInfoTO.getUserId(), payment, authorisationId, scaInfoTO.getAuthCode(), paymentKeyData.template());
        if (scaOperationService.authenticationCompleted(paymentId, opType)) {
            if (opType == PAYMENT) {
                paymentService.updatePaymentStatus(paymentId, ACTC);
                payment.setTransactionStatus(paymentService.executePayment(paymentId, scaInfoTO.getUserLogin()));
            } else {
                payment.setTransactionStatus(paymentService.cancelPayment(paymentId));
            }
        } else if (multilevelScaEnable) {
            payment.setTransactionStatus(paymentService.updatePaymentStatus(paymentId, PATC));
        }
        BearerTokenTO bearerToken = paymentAccountAccessToken(scaInfoTO, payment.getDebtorAccount().getIban());
        UserBO userBO = scaUtils.userBO(scaInfoTO.getUserId());
        SCAPaymentResponseTO response = new SCAPaymentResponseTO();
        int scaWeight = accessService.resolveScaWeightByDebtorAccount(userBO.getAccountAccesses(), payment.getDebtorAccount().getIban());
        scaResponseResolver.updateScaResponseFields(userBO, response, authorisationId, paymentKeyData.template(), bearerToken, FINALISED, scaWeight);
        return scaResponseResolver.updatePaymentRelatedResponseFields(response, payment);
    }

    @Override
    public SCAPaymentResponseTO selectSCAMethodForPayment(ScaInfoTO scaInfoTO, String paymentId) {
        return selectSCAMethod(scaInfoTO, paymentId, null, PAYMENT);
    }

    @Override
    public SCAPaymentResponseTO selectSCAMethodForCancelPayment(ScaInfoTO scaInfoTO, String paymentId, String cancellationId) {
        return selectSCAMethod(scaInfoTO, paymentId, cancellationId, CANCEL_PAYMENT);
    }

    private SCAPaymentResponseTO selectSCAMethod(ScaInfoTO scaInfoTO, String paymentId, String cancellationId, OpTypeBO opType) {
        UserBO userBO = scaUtils.userBO(scaInfoTO.getUserId());
        PaymentBO payment = loadPayment(paymentId);
        int scaWeight = accessService.resolveScaWeightByDebtorAccount(userBO.getAccountAccesses(), payment.getDebtorAccount().getIban());

        PaymentCoreDataTO paymentKeyData = coreDataPolicy.getPaymentCoreData(opType, payment);
        String template = paymentKeyData.template();
        String authorisationId = opType == PAYMENT ? scaInfoTO.getAuthorisationId() : cancellationId;
        SCAPaymentResponseTO response = new SCAPaymentResponseTO();
        scaResponseResolver.generateCodeAndUpdateResponse(paymentId, response, authorisationId, template, scaWeight, userBO, opType, scaInfoTO.getScaMethodId());

        BearerTokenTO bearerToken = paymentAccountAccessToken(scaInfoTO, payment.getDebtorAccount().getIban());
        scaResponseResolver.updateScaResponseFields(userBO, response, authorisationId, template, bearerToken, SCAMETHODSELECTED, scaWeight);
        return scaResponseResolver.updatePaymentRelatedResponseFields(response, payment);
    }

    @Override
    public SCAPaymentResponseTO loadSCAForPaymentData(ScaInfoTO scaInfoTO, String paymentId) {
        return loadSca(scaInfoTO, paymentId, scaInfoTO.getAuthorisationId(), PAYMENT);
    }

    @Override
    public SCAPaymentResponseTO loadSCAForCancelPaymentData(ScaInfoTO scaInfoTO, String paymentId, String cancellationId) {
        return loadSca(scaInfoTO, paymentId, cancellationId, CANCEL_PAYMENT);
    }

    private SCAPaymentResponseTO loadSca(ScaInfoTO scaInfoTO, String paymentId, String operationId, OpTypeBO opType) {
        SCAOperationBO scaOperation = scaOperationService.loadAuthCode(operationId);
        UserBO user = scaUtils.userBO(scaInfoTO.getUserId());
        PaymentBO payment = loadPayment(paymentId);
        PaymentCoreDataTO paymentKeyData = coreDataPolicy.getPaymentCoreData(opType, payment);
        BearerTokenTO bearerToken = paymentAccountAccessToken(scaInfoTO, payment.getDebtorAccount().getIban());
        int scaWeight = accessService.resolveScaWeightByDebtorAccount(user.getAccountAccesses(), payment.getDebtorAccount().getIban());

        SCAPaymentResponseTO response = new SCAPaymentResponseTO();
        scaResponseResolver.updateScaResponseFields(user, response, operationId, paymentKeyData.template(), bearerToken, ScaStatusTO.valueOf(scaOperation.getScaStatus().name()), scaWeight);
        scaResponseResolver.updateScaUserDataInResponse(user, scaOperation, response);
        return scaResponseResolver.updatePaymentRelatedResponseFields(response, payment);
    }

    private BearerTokenTO paymentAccountAccessToken(ScaInfoTO scaInfoTO, String iban) {
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
        aisConsent.setUserId(scaInfoTO.getUserLogin());
        return bearerTokenMapper.toBearerTokenTO(authorizationService.consentToken(scaInfoMapper.toScaInfoBO(scaInfoTO), aisConsent));
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

    private boolean scaRequired(UserBO user) {
        return scaUtils.hasSCA(user);
    }

    private DepositAccountBO checkAccountStatusAndCurrencyMatch(AccountReferenceBO reference) {
        DepositAccountBO account = Optional.ofNullable(reference.getCurrency())
                                           .map(c -> accountService.getAccountByIbanAndCurrency(reference.getIban(), c))
                                           .orElseGet(() -> getAccountByIbanErrorIfNotSingle(reference.getIban()));

        if (!account.isEnabled()) {
            throw MiddlewareModuleException.builder()
                          .errorCode(ACCOUNT_DISABLED)
                          .devMsg(String.format("Account with IBAN: %s is %s", reference.getIban(), account.getAccountStatus()))
                          .build();
        }
        return account;
    }

    private DepositAccountBO getAccountByIbanErrorIfNotSingle(String iban) {
        List<DepositAccountBO> accounts = accountService.getAccountsByIbanAndParamCurrency(iban, "");
        if (accounts.size() != 1) {
            String msg = CollectionUtils.isEmpty(accounts)
                                 ? String.format("Account with IBAN: %s Not Found!", iban)
                                 : String.format("Initiation of payment for Account with IBAN: %s is impossible as it is a Multi-Currency-Account. %nPlease specify currency for sub-account to proceed.", iban);
            MiddlewareErrorCode errorCode = CollectionUtils.isEmpty(accounts)
                                                    ? PAYMENT_PROCESSING_FAILURE
                                                    : CURRENCY_MISMATCH;
            throw MiddlewareModuleException.builder()
                          .errorCode(errorCode)
                          .devMsg(msg)
                          .build();
        }
        return accounts.iterator().next();
    }
}
