/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

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
import de.adorsys.ledgers.sca.domain.SCAOperationBO;
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

    @Value("${ledgers.sca.multilevel.enabled:false}")
    private boolean multilevelScaEnable;

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

    public <T extends SCAResponseTO> void updateScaResponseFields(UserBO user, T response, String authorisationId, String psuMessage, BearerTokenTO token, ScaStatusTO scaStatus, int scaWeight) {
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
