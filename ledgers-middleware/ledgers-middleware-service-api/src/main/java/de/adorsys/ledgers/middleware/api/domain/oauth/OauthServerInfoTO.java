/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.api.domain.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OauthServerInfoTO {
    @JsonProperty("authorization_endpoint")
    private String authorizationEndpoint;

    @JsonProperty("token_endpoint")
    private String tokenEndpoint;

    @JsonProperty("response_types_supported")
    private List<ResponseTypeTO> responseTypesSupported;

    @JsonProperty("grant_types_supported")
    private List<GrantTypeTO> grantTypesSupported;
}