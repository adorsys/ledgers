package de.adorsys.ledgers.um.api.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BearerTokenBO {

    private String access_token;
    private String token_type = "Bearer";
    private int expires_in;
    private String refresh_token;
    private AccessTokenBO accessTokenObject;
}
