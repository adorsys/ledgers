package de.adorsys.ledgers.middleware.impl.converter;

import de.adorsys.ledgers.deposit.api.domain.PaymentBO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTypeTO;
import de.adorsys.ledgers.middleware.api.domain.payment.TransactionStatusTO;
import de.adorsys.ledgers.middleware.api.domain.sca.SCAPaymentResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.SCAResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaDataInfoTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaStatusTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.ScaUserDataTO;
import de.adorsys.ledgers.middleware.api.domain.um.TokenUsageTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.service.ScaChallengeDataResolver;
import de.adorsys.ledgers.middleware.impl.service.SCAUtils;
import de.adorsys.ledgers.sca.domain.AuthCodeDataBO;
import de.adorsys.ledgers.sca.domain.OpTypeBO;
import de.adorsys.ledgers.sca.domain.SCAOperationBO;
import de.adorsys.ledgers.sca.domain.ScaStatusBO;
import de.adorsys.ledgers.sca.service.SCAOperationService;
import de.adorsys.ledgers.um.api.domain.UserBO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScaResponseResolver {
    private final SCAUtils scaUtils;
    private final ScaChallengeDataResolver scaChallengeDataResolver;
    private final UserMapper userMapper;
    private final SCAOperationService scaOperationService;

    @Value("${sca.multilevel.enabled:false}")
    private boolean multilevelScaEnable;
    @Value("${default.token.lifetime.seconds:600}")
    private int defaultLoginTokenExpireInSeconds;

    public <T extends SCAResponseTO> void completeResponse(T response, SCAOperationBO operation, UserTO user, String template, BearerTokenTO token) {
        response.setScaStatus(ScaStatusTO.valueOf(operation.getScaStatus().name()));
        response.setAuthorisationId(operation.getId());
        response.setScaMethods(user.getScaUserData());
        response.setStatusDate(operation.getStatusTime());
        response.setExpiresInSeconds(operation.getValiditySeconds());
        ScaUserDataTO userData = scaUtils.getScaMethod(user, operation.getScaMethodId());
        response.setChosenScaMethod(userData);
        if (userData != null) {
            response.setChallengeData(scaChallengeDataResolver.resolveScaChallengeData(userData.getScaMethod())
                                              .getChallengeData(new ScaDataInfoTO(userData, operation.getTan())));
        }
        response.setPsuMessage(template);
        response.setBearerToken(token);
    }

    public ScaStatusTO resolveScaStatus(TokenUsageTO tokenUsage, boolean isScaRequired) {
        if (isScaRequired) {
            return TokenUsageTO.DELEGATED_ACCESS.equals(tokenUsage)
                           ? ScaStatusTO.PSUIDENTIFIED
                           : ScaStatusTO.PSUAUTHENTICATED;
        }
        return ScaStatusTO.EXEMPTED;
    }

    public void prepareScaAndUpdateResponse(String paymentId, SCAPaymentResponseTO response, String authorisationId, String psuMessage, int scaWeight, UserBO user, OpTypeBO opType) {
        AuthCodeDataBO authCodeData = new AuthCodeDataBO(user.getLogin(), null, paymentId, psuMessage, psuMessage, defaultLoginTokenExpireInSeconds, opType, authorisationId, scaWeight);
        SCAOperationBO operation = scaOperationService.createAuthCode(authCodeData, ScaStatusBO.valueOf(response.getScaStatus().name()));
        updateScaUserDataInResponse(user, operation, response);
    }

    public void generateCodeAndUpdateResponse(String paymentId, SCAPaymentResponseTO response, String authorisationId, String psuMessage, int scaWeight, UserBO user, OpTypeBO opType, String scaMethodId) {
        AuthCodeDataBO authCodeData = new AuthCodeDataBO(user.getLogin(), scaMethodId, paymentId, psuMessage, psuMessage, defaultLoginTokenExpireInSeconds, opType, authorisationId, scaWeight);
        SCAOperationBO operation = scaOperationService.generateAuthCode(authCodeData, user, ScaStatusBO.SCAMETHODSELECTED);
        updateScaUserDataInResponse(user, operation, response);
    }

    public void updateScaUserDataInResponse(UserBO user, SCAOperationBO operation, SCAResponseTO response) {
        ScaUserDataTO userData = scaUtils.getScaMethod(user, operation.getScaMethodId());
        response.setChosenScaMethod(userData);
        response.setStatusDate(operation.getStatusTime());
        response.setExpiresInSeconds(operation.getValiditySeconds());
        if (userData != null) {
            response.setChallengeData(scaChallengeDataResolver.resolveScaChallengeData(userData.getScaMethod())
                                              .getChallengeData(new ScaDataInfoTO(userData, operation.getTan())));
        }
    }

    public void updateScaResponseFields(UserBO user, SCAPaymentResponseTO response, String authorisationId, String psuMessage, BearerTokenTO token, ScaStatusTO scaStatus, int scaWeight) {
        response.setScaStatus(scaStatus);
        response.setAuthorisationId(authorisationId);
        response.setScaMethods(userMapper.toScaUserDataListTO(user.getScaUserData()));
        response.setPsuMessage(psuMessage);
        response.setBearerToken(token);
        response.setMultilevelScaRequired(multilevelScaEnable && scaWeight < 100);
    }

    public SCAPaymentResponseTO updatePaymentRelatedResponseFields(SCAPaymentResponseTO response, PaymentBO payment) {
        response.setPaymentId(payment.getPaymentId());
        response.setTransactionStatus(TransactionStatusTO.valueOf(payment.getTransactionStatus().name()));
        response.setPaymentType(PaymentTypeTO.valueOf(payment.getPaymentType().name()));
        response.setPaymentProduct(payment.getPaymentProduct());
        return response;
    }
}
