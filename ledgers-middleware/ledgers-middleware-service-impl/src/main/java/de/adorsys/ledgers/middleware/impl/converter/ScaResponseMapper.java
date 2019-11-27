package de.adorsys.ledgers.middleware.impl.converter;

import de.adorsys.ledgers.middleware.api.domain.sca.SCAResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaDataInfoTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaStatusTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.ScaUserDataTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.service.ScaChallengeDataResolver;
import de.adorsys.ledgers.middleware.impl.service.SCAUtils;
import de.adorsys.ledgers.sca.domain.SCAOperationBO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ScaResponseMapper {
    private final SCAUtils scaUtils;
    private final ScaChallengeDataResolver scaChallengeDataResolver;

    public <T extends SCAResponseTO> void completeResponse(T response, SCAOperationBO operation, UserTO user, String template, BearerTokenTO token) {
        response.setAuthorisationId(operation.getId());
        ScaUserDataTO userData = scaUtils.getScaMethod(user, operation.getScaMethodId());
        response.setChosenScaMethod(userData);
        if (userData != null) {
            response.setChallengeData(scaChallengeDataResolver.resolveScaChallengeData(userData.getScaMethod())
                                              .getChallengeData(new ScaDataInfoTO(userData, operation.getTan())));
        }
        response.setExpiresInSeconds(operation.getValiditySeconds());
        response.setScaMethods(user.getScaUserData());
        response.setStatusDate(operation.getStatusTime());
        response.setScaStatus(ScaStatusTO.valueOf(operation.getScaStatus().name()));
        response.setPsuMessage(template);
        response.setBearerToken(token);
    }
}
