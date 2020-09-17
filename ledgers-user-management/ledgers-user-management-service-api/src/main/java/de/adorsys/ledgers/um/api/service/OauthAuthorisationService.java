package de.adorsys.ledgers.um.api.service;

import de.adorsys.ledgers.um.api.domain.oauth.OauthCodeResponseBO;
import de.adorsys.ledgers.um.api.domain.oauth.OauthServerInfoBO;

public interface OauthAuthorisationService {
    /**
     * @param userId User id
     * @return OauthCodeResponseBO representation of oauth code
     */
    OauthCodeResponseBO oauthCode(String userId, String accessToken);

    /**
     * @param code Oauth code
     * @return OauthTokenResponseBO representation of oauth token
     */
    String oauthToken(String code);

    /**
     * @return OauthServerInfoBO Authorization server metadata request
     */
    OauthServerInfoBO oauthServerInfo();
}
