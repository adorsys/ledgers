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

import de.adorsys.ledgers.deposit.api.domain.*;
import de.adorsys.ledgers.deposit.api.service.DepositAccountPaymentService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.keycloak.client.api.KeycloakTokenService;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentCoreDataTO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTypeTO;
import de.adorsys.ledgers.middleware.api.domain.payment.TransactionStatusTO;
import de.adorsys.ledgers.middleware.api.domain.sca.SCAPaymentResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaStatusTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareErrorCode;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareModuleException;
import de.adorsys.ledgers.middleware.api.service.MiddlewarePaymentService;
import de.adorsys.ledgers.middleware.impl.config.PaymentProductsConfig;
import de.adorsys.ledgers.middleware.impl.converter.AccountDetailsMapper;
import de.adorsys.ledgers.middleware.impl.converter.PaymentConverter;
import de.adorsys.ledgers.middleware.impl.converter.ScaResponseResolver;
import de.adorsys.ledgers.middleware.impl.policies.PaymentCancelPolicy;
import de.adorsys.ledgers.middleware.impl.policies.PaymentCoreDataPolicy;
import de.adorsys.ledgers.sca.domain.OpTypeBO;
import de.adorsys.ledgers.sca.service.SCAOperationService;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.util.Ids;
import de.adorsys.ledgers.util.exception.DepositModuleException;
import de.adorsys.ledgers.util.exception.ScaModuleException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.Currency;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO.*;
import static de.adorsys.ledgers.middleware.api.domain.Constants.SCOPE_FULL_ACCESS;
import static de.adorsys.ledgers.middleware.api.exception.MiddlewareErrorCode.*;
import static de.adorsys.ledgers.middleware.api.exception.MiddlewareModuleException.blockedSupplier;
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
    private final SCAUtils scaUtils;
    private final PaymentCoreDataPolicy coreDataPolicy;
    private final AccessService accessService;
    private final ScaResponseResolver scaResponseResolver;
    private final PaymentProductsConfig paymentProductsConfig;
    private final AccountDetailsMapper detailsMapper;
    private final KeycloakTokenService tokenService;

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
    public SCAPaymentResponseTO initiatePayment(ScaInfoTO scaInfoTO, PaymentTO payment, PaymentTypeTO paymentType) {
        return checkPaymentAndPrepareResponse(scaInfoTO, paymentConverter.toPaymentBO(payment, paymentType));
    }

    private SCAPaymentResponseTO checkPaymentAndPrepareResponse(ScaInfoTO scaInfoTO, PaymentBO paymentBO) {
        validatePayment(paymentBO);
        paymentBO.updateDebtorAccountCurrency(getCheckedAccount(paymentBO).getCurrency());
        UserBO user = scaUtils.userBO(scaInfoTO.getUserLogin());
        TransactionStatusBO status = user.hasSCA()
                                             ? ACCP
                                             : ACTC;
        return prepareScaAndResolveResponse(scaInfoTO, persist(paymentBO, status), user, PAYMENT);
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

    private DepositAccountBO getCheckedAccount(PaymentBO paymentBO) { //TODO move to RequestValidationFilter
        DepositAccountBO debtorAccount = checkAccountStatusAndCurrencyMatch(paymentBO.getDebtorAccount(), true, null);
        paymentBO.setAccountId(debtorAccount.getId());
        paymentBO.getTargets()
                .forEach(t -> {
                    try {
                        DepositAccountBO acc = checkAccountStatusAndCurrencyMatch(t.getCreditorAccount(), false, t.getInstructedAmount().getCurrency());
                        t.setCreditorAccount(acc.getReference());
                    } catch (MiddlewareModuleException e) {
                        if (EnumSet.of(ACCOUNT_DISABLED, CURRENCY_MISMATCH).contains(e.getErrorCode())) {
                            log.error(e.getDevMsg());
                            throw e;
                        }
                    } catch (DepositModuleException e) {
                        log.info("Creditor account is located in another ASPSP");
                    }
                });
        return debtorAccount;
    }

    private void validatePayment(PaymentBO paymentBO) { //TODO move to RequestValidationFilter
        String msg = null;
        if (!paymentBO.isValidAmount()) {
            msg = "Instructed amount is invalid.";
        }

        if (paymentProductsConfig.isNotSupportedPaymentProduct(paymentBO.getPaymentProduct())) {
            msg = "Payment Product not Supported!";
        }
        if (msg != null) {
            throw MiddlewareModuleException.builder()
                          .devMsg(String.format("Payment validation failed! %s", msg))
                          .errorCode(REQUEST_VALIDATION_FAILURE)
                          .build();
        }
    }

    private SCAPaymentResponseTO prepareScaAndResolveResponse(ScaInfoTO scaInfoTO, PaymentBO payment, UserBO user, OpTypeBO opType) {
        boolean isScaRequired = user.hasSCA();
        String psuMessage = coreDataPolicy.getPaymentCoreData(opType, payment).resolveMessage(isScaRequired);
        BearerTokenTO token = accessService.exchangeTokenStartSca(isScaRequired, scaInfoTO.getAccessToken());
        ScaStatusTO scaStatus = isScaRequired
                                        ? ScaStatusTO.PSUAUTHENTICATED
                                        : ScaStatusTO.EXEMPTED;
        int scaWeight = user.resolveWeightForAccount(payment.getAccountId());
        SCAPaymentResponseTO response = new SCAPaymentResponseTO();
        scaResponseResolver.updateScaResponseFields(user, response, null, psuMessage, token, scaStatus, scaWeight);
        return scaResponseResolver.updatePaymentRelatedResponseFields(response, payment);
    }

    private PaymentBO persist(PaymentBO paymentBO, TransactionStatusBO status) {
        if (paymentBO.getPaymentId() == null) {
            paymentBO.setPaymentId(Ids.id());
        }
        return paymentService.initiatePayment(paymentBO, status);
    }

    @Override
    public PaymentTO getPaymentById(String paymentId) {
        PaymentBO paymentResult = paymentService.getPaymentById(paymentId);
        return paymentConverter.toPaymentTO(paymentResult);
    }

    @Override
    public String iban(String paymentId) { //TODO Consider removing!
        return paymentService.readIbanByPaymentId(paymentId);
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
        List<AccountReferenceBO> referenceList = detailsMapper.toAccountReferenceList(scaUtils.userBO(scaInfoTO.getUserLogin()).getAccountAccesses());
        List<PaymentBO> payments = paymentService.getPaymentsByTypeStatusAndDebtor(PaymentTypeBO.PERIODIC, ACSP, referenceList);
        return paymentConverter.toPaymentTOList(payments);
    }

    private SCAPaymentResponseTO authorizeOperation(ScaInfoTO scaInfoTO, String paymentId, OpTypeBO opType) {
        PaymentBO payment = paymentService.getPaymentById(paymentId);
        PaymentCoreDataTO paymentKeyData = coreDataPolicy.getPaymentCoreData(opType, payment);
        if (scaOperationService.authenticationCompleted(paymentId, opType)) {
            performExecuteOrCancelOperation(scaInfoTO, paymentId, opType, payment);
        } else if (multilevelScaEnable) {
            payment.setTransactionStatus(paymentService.updatePaymentStatus(paymentId, PATC));
        }
        UserBO user = scaUtils.userBO(scaInfoTO.getUserLogin());
        SCAPaymentResponseTO response = new SCAPaymentResponseTO();
        int scaWeight = user.resolveWeightForAccount(payment.getAccountId());
        scaResponseResolver.updateScaResponseFields(user, response, null, paymentKeyData.getTanTemplate(), null, ScaStatusTO.FINALISED, scaWeight);
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

    private DepositAccountBO checkAccountStatusAndCurrencyMatch(AccountReferenceBO reference, boolean isDebtor, Currency currency) {//TODO move to ValidationFilter
        DepositAccountBO account = Optional.ofNullable(reference.getCurrency())
                                           .map(c -> accountService.getAccountByIbanAndCurrency(reference.getIban(), c))
                                           .orElseGet(() -> getAccountByIbanAndParamCurrencyErrorIfNotSingle(reference.getIban(), isDebtor, currency));
        if (!account.isEnabled()) {
            throw blockedSupplier(ACCOUNT_DISABLED, reference.getIban(), account.isBlocked()).get();
        }
        return account;
    }

    private DepositAccountBO getAccountByIbanAndParamCurrencyErrorIfNotSingle(String iban, boolean isDebtor, Currency currency) {
        List<DepositAccountBO> accounts = accountService.getAccountsByIbanAndParamCurrency(iban, "");
        if (CollectionUtils.isEmpty(accounts) && !isDebtor) {
            return new DepositAccountBO(null, iban, null, null, null, null, currency, null, null, null, null, null, null, null, false, false, null, null);
        }
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
