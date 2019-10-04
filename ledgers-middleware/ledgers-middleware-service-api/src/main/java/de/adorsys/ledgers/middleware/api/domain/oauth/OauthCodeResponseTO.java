package de.adorsys.ledgers.middleware.api.domain.oauth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OauthCodeResponseTO {
    private String redirectUri;
}
