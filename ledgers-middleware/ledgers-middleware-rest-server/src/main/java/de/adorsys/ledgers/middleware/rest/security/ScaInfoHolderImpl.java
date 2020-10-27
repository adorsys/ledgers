package de.adorsys.ledgers.middleware.rest.security;

import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.um.AccessTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.TokenUsageTO;
import de.adorsys.ledgers.um.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScaInfoHolderImpl implements ScaInfoHolder {
    private final AccessTokenTO accessTokenTO;
    private final UserService userService;
    private final BearerTokenTO bearerTokenTO;

    @Override
    public String getUserId() {
        return userService.findByLogin(accessTokenTO.getLogin()).getId();
    }

    @Override
    public TokenUsageTO getTokenUsageTO() {
        return accessTokenTO.getTokenUsage();
    }

    @Override
    public ScaInfoTO getScaInfo() {
        return buildScaInfo();
    }

    @Override
    public ScaInfoTO getScaInfoWithScaMethodIdAndAuthorisationId(String scaMethodId, String authorizationId) {
        ScaInfoTO info = buildScaInfo();
        info.setScaMethodId(scaMethodId);
        info.setAuthorisationId(authorizationId);
        return info;
    }

    @Override
    public ScaInfoTO getScaInfoWithAuthCodeAndAuthorisationId(String authCode, String authorizationId) {
        ScaInfoTO info = buildScaInfo();
        info.setAuthCode(authCode);
        info.setAuthorisationId(authorizationId);
        return info;
    }

    private ScaInfoTO buildScaInfo() {
        ScaInfoTO info = new ScaInfoTO();
        info.setUserId(getUserId());
        info.setAuthorisationId(accessTokenTO.getAuthorisationId());
        info.setScaId(accessTokenTO.getScaId());
        info.setUserRole(accessTokenTO.getRole());
        info.setUserLogin(accessTokenTO.getLogin());
        info.setAccessToken(accessTokenTO.getAccessToken());
        info.setBearerToken(this.bearerTokenTO);
        return info;
    }

}
