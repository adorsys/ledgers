/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.api.domain.um;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BearerTokenTO {

    private String access_token;

    private String token_type = "Bearer";

    private int expires_in;

    private String refresh_token;

    private AccessTokenTO accessTokenObject;

    private Set<String> scopes;

}
