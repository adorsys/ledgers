package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.deposit.api.domain.PaymentBO;
import de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO;
import de.adorsys.ledgers.deposit.api.service.DepositAccountPaymentService;
import de.adorsys.ledgers.middleware.api.domain.sca.GlobalScaResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaLoginOprTO;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareModuleException;
import de.adorsys.ledgers.middleware.api.service.MiddlewareScaService;
import de.adorsys.ledgers.middleware.impl.converter.ScaInfoMapper;
import de.adorsys.ledgers.middleware.impl.converter.ScaResponseConverter;
import de.adorsys.ledgers.sca.domain.*;
import de.adorsys.ledgers.sca.service.SCAOperationService;
import de.adorsys.ledgers.um.api.domain.*;
import de.adorsys.ledgers.um.api.service.AuthorizationService;
import de.adorsys.ledgers.um.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import static de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO.ACTC;
import static de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO.PATC;
import static de.adorsys.ledgers.middleware.api.exception.MiddlewareErrorCode.INSUFFICIENT_PERMISSION;
import static de.adorsys.ledgers.sca.domain.OpTypeBO.*;

@Service
@RequiredArgsConstructor
public class MiddlewareScaServiceImpl implements MiddlewareScaService {
    private static final String NO_USER_MESSAGE = "No user message";

    @Value("${ledgers.default.token.lifetime.seconds:600}")
    private int defaultLoginTokenExpireInSeconds;

    @Value("${ledgers.sca.multilevel.enabled:false}")
    private boolean multilevelScaEnable;

    private final UserService userService;
    private final DepositAccountPaymentService paymentService;
    private final SCAOperationService scaOperationService;
    private final AccessService accessService;
    private final ScaInfoMapper scaInfoMapper;
    private final AuthorizationService authorizationService;
    private final ScaResponseConverter scaResponseConverter;
    private final ScaResponseMessageResolver messageResolver;

    @Override
    public GlobalScaResponseTO loginForOperation(ScaLoginOprTO loginOpr) {
        OpTypeBO opTypeBO = valueOf(loginOpr.getOpType().name());
        UserBO user = userService.findByLogin(loginOpr.getLogin());
        AuthCodeDataBO codeData = new AuthCodeDataBO(user.getLogin(), null, loginOpr.getOprId(), NO_USER_MESSAGE,
                                                     defaultLoginTokenExpireInSeconds, opTypeBO, loginOpr.getAuthorisationId(), 0);
        SCAOperationBO operation = scaOperationService.checkIfExistsOrNew(codeData);

        try {
            BearerTokenBO token = proceedToLogin(loginOpr);
            return resolveLoginResponseForConsentLogin(operation, user, token);
        } catch (MiddlewareModuleException e) {
            throw scaOperationService.updateFailedCount(operation.getId(), true);
        }
    }

    @Override
    public GlobalScaResponseTO getMethods(String authorizationId, String userId) {
        UserBO user = userService.findById(userId);
        SCAOperationBO operation = scaOperationService.loadAuthCode(authorizationId);
        return scaResponseConverter.mapResponse(operation, user.getScaUserData(), null, messageResolver.updateMessage(null, operation), null, 0, null);
    }

    @Override
    public GlobalScaResponseTO selectMethod(ScaInfoTO scaInfo) {
        SCAOperationBO operation = scaOperationService.loadAuthCode(scaInfo.getAuthorisationId());
        UserBO user = userService.findById(scaInfo.getUserId());
        String psuMessage = messageResolver.getTemplate(operation);
        int scaWeight = getScaWeight(operation.getOpType(), operation.getOpId(), user);
        AuthCodeDataBO data = new AuthCodeDataBO(user.getLogin(), scaInfo.getScaMethodId(), operation.getOpId(), psuMessage, defaultLoginTokenExpireInSeconds, operation.getOpType(), operation.getId(), scaWeight);
        operation = scaOperationService.generateAuthCode(data, user, ScaStatusBO.SCAMETHODSELECTED);
        return scaResponseConverter.mapResponse(operation, user.getScaUserData(), operation.getOpType() == PAYMENT
                                                                                          ? paymentService.getPaymentStatusById(operation.getOpId())
                                                                                          : null,
                                                messageResolver.updateMessage(psuMessage, operation), null, scaWeight, null);
    }

    @Override
    public GlobalScaResponseTO confirmAuthorization(ScaInfoTO scaInfo) {
        SCAOperationBO operation = scaOperationService.loadAuthCode(scaInfo.getAuthorisationId());
        UserBO user = userService.findById(scaInfo.getUserId());

        int scaWeight = getScaWeight(operation.getOpType(), operation.getOpId(), user);
        String psuMessage = messageResolver.getTemplate(operation);
        ScaValidationBO scaValidation = scaOperationService.validateAuthCode(scaInfo.getAuthorisationId(), operation.getOpId(), scaInfo.getAuthCode(), scaWeight);
        operation.setScaStatus(scaValidation.getScaStatus());

        TransactionStatusBO transactionStatus = EnumSet.of(PAYMENT, CANCEL_PAYMENT).contains(operation.getOpType())
                                                        ? performPaymentConfirmation(operation, scaInfo.getUserLogin())
                                                        : null;

        BearerTokenBO token = resolveTokenConfirmation(scaInfoMapper.toScaInfoBO(scaInfo), operation);
        return scaResponseConverter.mapResponse(operation, user.getScaUserData(), transactionStatus, messageResolver.updateMessage(psuMessage, operation), token, scaWeight, scaValidation.getAuthConfirmationCode());
    }

    private BearerTokenBO resolveTokenConfirmation(ScaInfoBO scaInfo, SCAOperationBO operation) {
        AisConsentBO consent = operation.getOpType() == CONSENT
                                       ? userService.loadConsent(operation.getOpId())
                                       : getAisConsentFromPayment(operation, scaInfo.getUserId());
        return authorizationService.consentToken(scaInfo, consent);
    }

    private AisConsentBO getAisConsentFromPayment(SCAOperationBO operation, String userLogin) {
        PaymentBO payment = paymentService.getPaymentById(operation.getOpId());
        return new AisConsentBO(payment.getDebtorAccount().getIban(), 0, true, userLogin);
    }

    private TransactionStatusBO performPaymentConfirmation(SCAOperationBO operation, String userLogin) {
        //TODO refactor this!
        boolean authenticationCompleted = scaOperationService.authenticationCompleted(operation.getOpId(), operation.getOpType());
        if (authenticationCompleted) {
            if (operation.getOpType() == PAYMENT) {
                paymentService.updatePaymentStatus(operation.getOpId(), ACTC);
                return paymentService.executePayment(operation.getOpId(), userLogin);
            } else {
                return paymentService.cancelPayment(operation.getOpId());
            }
        } else if (multilevelScaEnable) {
            return paymentService.updatePaymentStatus(operation.getOpId(), PATC);
        }
        return paymentService.getPaymentById(operation.getOpId()).getTransactionStatus();
    }

    private GlobalScaResponseTO resolveLoginResponseForConsentLogin(SCAOperationBO operation, UserBO user, BearerTokenBO token) {
        if (!user.hasSCA()) {
            operation.setScaStatus(ScaStatusBO.EXEMPTED);
            return scaResponseConverter.mapResponse(operation, user.getScaUserData(), null, messageResolver.updateMessage(null, operation), token, 100, null);
        } else {
            AuthCodeDataBO authCodeData = new AuthCodeDataBO(user.getLogin(), null, operation.getOpId(), NO_USER_MESSAGE,
                                                             defaultLoginTokenExpireInSeconds, operation.getOpType(), operation.getId(), 100);
            SCAOperationBO scaOperationBO = scaOperationService.createAuthCode(authCodeData, ScaStatusBO.PSUIDENTIFIED);
            return scaResponseConverter.mapResponse(scaOperationBO, user.getScaUserData(), null, messageResolver.updateMessage(null, operation), token, 100, null);
        }
    }

    private int getScaWeight(OpTypeBO opType, String oprId, UserBO user) {
        return opType == LOGIN
                       ? 100
                       : resolveWeightForOperation(opType, oprId, user);
    }

    private int resolveWeightForOperation(OpTypeBO opType, String oprId, UserBO user) {
        Set<String> ibans = opType == CONSENT
                                    ? userService.loadConsent(oprId).getAccess().getAllAccounts()
                                    : Collections.singleton(paymentService.getPaymentById(oprId).getDebtorAccount().getIban());
        return accessService.resolveScaWeightCommon(ibans, user.getAccountAccesses());
    }

    private BearerTokenBO proceedToLogin(ScaLoginOprTO loginOpr) {
        // For login we use the login name and login time for authId and authorizationId.
        BearerTokenBO loginToken = Optional.ofNullable(authorizationService.authorise(loginOpr.getLogin(), loginOpr.getPin(), UserRoleBO.CUSTOMER, loginOpr.getOprId(), loginOpr.getAuthorisationId()))
                                           .orElseThrow(() -> MiddlewareModuleException.builder()
                                                                      .errorCode(INSUFFICIENT_PERMISSION)
                                                                      .devMsg("Unknown credentials.")
                                                                      .build());
        return authorizationService.scaToken(loginToken.getAccessTokenObject().buildScaInfoBO());
    }
}
