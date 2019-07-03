package de.adorsys.ledgers.middleware.rest.security;

import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.um.AccessTokenTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticationFacadeImpl implements AuthenticationFacade {
    private final AccessTokenTO accessTokenTO;

    @Override
    public String getUserId() {
        return accessTokenTO.getSub();
    }

    @Override
    public ScaInfoTO getScaInfoWithScaMethodId(String scaMethodId) {
        ScaInfoTO info = buildScaInfo();
        info.setScaMethodId(scaMethodId);
        return info;
    }

    @Override
    public ScaInfoTO getScaInfoWithAuthCode(String authCode) {
        ScaInfoTO info = buildScaInfo();
        info.setAuthCode(authCode);
        return info;
    }

    private ScaInfoTO buildScaInfo() {
        ScaInfoTO info = new ScaInfoTO();
        info.setUserId(accessTokenTO.getSub());
        info.setAuthorisationId(accessTokenTO.getAuthorisationId());
        info.setScaId(accessTokenTO.getScaId());
        info.setUserRole(accessTokenTO.getRole());
        return info;
    }

}
