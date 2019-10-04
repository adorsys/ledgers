package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.middleware.api.domain.oauth.OauthCodeResponseTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.service.MiddlewareOauthService;
import de.adorsys.ledgers.middleware.impl.converter.BearerTokenMapper;
import de.adorsys.ledgers.um.api.domain.oauth.OauthCodeResponseBO;
import de.adorsys.ledgers.um.api.domain.oauth.OauthTokenResponseBO;
import de.adorsys.ledgers.um.api.service.OauthAuthorisationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MiddlewareOauthServiceImpl implements MiddlewareOauthService {
    private static final String OAUTH_CODE_PARAM = "?code=";

    private final BearerTokenMapper bearerTokenMapper;
    private final OauthAuthorisationService oauthAuthorisationService;

    @Override
    public OauthCodeResponseTO oauthCode(String login, String pin, String redirectUri) {
        OauthCodeResponseBO response = oauthAuthorisationService.oauthCode(login, pin);
        return new OauthCodeResponseTO(redirectUri + OAUTH_CODE_PARAM + response.getCode());
    }

    @Override
    public BearerTokenTO oauthToken(String code) {
        OauthTokenResponseBO response = oauthAuthorisationService.oauthToken(code);
        return bearerTokenMapper.toBearerTokenTO(response.getBearerTokenBO());
    }
}
