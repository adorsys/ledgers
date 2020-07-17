package de.adorsys.ledgers.middleware.rest.security;

import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.um.AccessTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.TokenUsageTO;
import de.adorsys.ledgers.um.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScaInfoHolderImpl implements ScaInfoHolder {
    private final AccessTokenTO accessTokenTO;
    private final UserService userService;

    @Override
    public String getUserId() {
        return userService.findByLogin(accessTokenTO.getLogin()/*getSub()*/).getId();
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
        info.setUserLogin(accessTokenTO.getLogin());
        return info;
    }

}
