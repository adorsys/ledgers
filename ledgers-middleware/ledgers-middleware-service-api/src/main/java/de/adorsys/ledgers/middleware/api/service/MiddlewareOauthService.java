package de.adorsys.ledgers.middleware.api.service;

import de.adorsys.ledgers.middleware.api.domain.oauth.OauthCodeResponseTO;
import de.adorsys.ledgers.middleware.api.domain.oauth.OauthServerInfoTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;

public interface MiddlewareOauthService {
    OauthCodeResponseTO oauthCode(String login, String pin, String redirectUri);

    OauthCodeResponseTO oauthCode(String userId, String redirectUri);

    BearerTokenTO oauthToken(String code);

    OauthServerInfoTO oauthServerInfo();
}
