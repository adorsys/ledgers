/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.api.domain.oauth;

import lombok.Data;

@Data
public class OauthCodeResponseTO {
    private String redirectUri;

    public OauthCodeResponseTO(String redirectUriBase, String code) {
        String paramPrefix = redirectUriBase.contains("?") ? "&" : "?";
        this.redirectUri = redirectUriBase + paramPrefix + "code=" + code;
    }
}
