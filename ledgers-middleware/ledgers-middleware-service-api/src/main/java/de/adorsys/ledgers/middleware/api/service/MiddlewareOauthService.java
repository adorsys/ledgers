/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.api.service;

import de.adorsys.ledgers.middleware.api.domain.oauth.OauthCodeResponseTO;
import de.adorsys.ledgers.middleware.api.domain.oauth.OauthServerInfoTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;

public interface MiddlewareOauthService { //TODO Shall be removed after final migration to Keycloak
    OauthCodeResponseTO oauthCode(String login, String pin, String redirectUri);

    OauthCodeResponseTO oauthCode(ScaInfoTO scaInfo, String redirectUri);

    BearerTokenTO oauthToken(String code);

    OauthServerInfoTO oauthServerInfo();
}
