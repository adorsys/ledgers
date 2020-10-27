package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.keycloak.client.api.KeycloakTokenService;
import de.adorsys.ledgers.middleware.api.domain.oauth.OauthCodeResponseTO;
import de.adorsys.ledgers.middleware.api.domain.oauth.OauthServerInfoTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.service.MiddlewareOauthService;
import de.adorsys.ledgers.middleware.impl.converter.OauthServerInfoMapper;
import de.adorsys.ledgers.um.api.domain.oauth.OauthCodeResponseBO;
import de.adorsys.ledgers.um.api.domain.oauth.OauthTokenHolder;
import de.adorsys.ledgers.um.api.service.OauthAuthorisationService;
import de.adorsys.ledgers.um.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static de.adorsys.ledgers.middleware.api.domain.Constants.SCOPE_FULL_ACCESS;

@Service
@RequiredArgsConstructor
public class MiddlewareOauthServiceImpl implements MiddlewareOauthService {
    private final OauthAuthorisationService oauthAuthorisationService;
    private final OauthServerInfoMapper oauthServerInfoMapper;
    private final KeycloakTokenService tokenService;
    private final UserService userService;

    @Value("${ledgers.token.lifetime.seconds.full:7776000}")
    private int fullTokenLifeTime;

    @Override
    public OauthCodeResponseTO oauthCode(String login, String pin, String redirectUri) {
        BearerTokenTO token = tokenService.login(login, pin);
        String userId = userService.findByLogin(login).getId();
        OauthCodeResponseBO response = oauthAuthorisationService.oauthCode(userId, token.getAccess_token(), false);
        return new OauthCodeResponseTO(redirectUri, response.getCode());
    }

    @Override
    public OauthCodeResponseTO oauthCode(ScaInfoTO scaInfo, String redirectUri) {
        OauthCodeResponseBO response = oauthAuthorisationService.oauthCode(scaInfo.getUserId(), scaInfo.getAccessToken(), true);
        return new OauthCodeResponseTO(redirectUri, response.getCode());
    }

    @Override
    public BearerTokenTO oauthToken(String code) {
        OauthTokenHolder holder = oauthAuthorisationService.oauthToken(code);
        return holder.isFinalStage()
                       ? tokenService.exchangeToken(holder.getOldToken(), fullTokenLifeTime, SCOPE_FULL_ACCESS)
                       : tokenService.validate(holder.getOldToken());
    }

    @Override
    public OauthServerInfoTO oauthServerInfo() {
        return oauthServerInfoMapper.toOauthServerInfoTO(oauthAuthorisationService.oauthServerInfo());
    }
}
