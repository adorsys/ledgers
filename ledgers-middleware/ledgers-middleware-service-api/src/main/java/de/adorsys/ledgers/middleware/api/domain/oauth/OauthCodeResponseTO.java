package de.adorsys.ledgers.middleware.api.domain.oauth;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OauthCodeResponseTO {
    private String redirectUri;

    public OauthCodeResponseTO(String redirectUriBase, String code) {
        String paramPrefix = redirectUriBase.contains("?") ? "&" : "?";
        this.redirectUri = redirectUriBase + paramPrefix + "code=" + code;
    }
}
