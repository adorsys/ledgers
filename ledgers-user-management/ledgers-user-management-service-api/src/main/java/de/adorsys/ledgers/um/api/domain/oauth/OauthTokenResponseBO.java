/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.um.api.domain.oauth;

import de.adorsys.ledgers.um.api.domain.BearerTokenBO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OauthTokenResponseBO {
    private BearerTokenBO bearerTokenBO;
}