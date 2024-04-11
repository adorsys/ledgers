/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.um.api.domain.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OauthServerInfoBO {
    @JsonProperty("authorization_endpoint")
    private String authorizationEndpoint;

    @JsonProperty("token_endpoint")
    private String tokenEndpoint;

    @JsonProperty("response_types_supported")
    private List<ResponseTypeBO> responseTypesSupported;

    @JsonProperty("grant_types_supported")
    private List<GrantTypeBO> grantTypesSupported;
}
