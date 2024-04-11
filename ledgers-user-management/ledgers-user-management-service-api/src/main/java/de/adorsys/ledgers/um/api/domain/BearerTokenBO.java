/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.um.api.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
public class BearerTokenBO {

    private String access_token;
    private String token_type = "Bearer";
    private int expires_in;
    private String refresh_token;
    private AccessTokenBO accessTokenObject;
    private Set<String> scopes;
}
