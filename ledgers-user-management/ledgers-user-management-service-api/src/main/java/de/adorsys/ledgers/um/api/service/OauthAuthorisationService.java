/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.um.api.service;

import de.adorsys.ledgers.um.api.domain.oauth.OauthCodeResponseBO;
import de.adorsys.ledgers.um.api.domain.oauth.OauthServerInfoBO;
import de.adorsys.ledgers.um.api.domain.oauth.OauthTokenHolder;

public interface OauthAuthorisationService {
    /**
     * @param userId User id
     * @return OauthCodeResponseBO representation of oauth code
     */
    OauthCodeResponseBO oauthCode(String userId, String accessToken, boolean finalStage);

    /**
     * @param code Oauth code
     * @return OauthTokenResponseBO representation of oauth token
     */
    OauthTokenHolder oauthToken(String code);

    /**
     * @return OauthServerInfoBO Authorization server metadata request
     */
    OauthServerInfoBO oauthServerInfo();
}
