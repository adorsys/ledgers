/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.api.domain.sca;

import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.TokenUsageTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;

// TODO create core-api and remove this class https://git.adorsys.de/adorsys/xs2a/ledgers/issues/230
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScaInfoTO {
    private String userId;
    private String scaId;
    private String authorisationId;
    private UserRoleTO userRole;
    private String scaMethodId;
    private String authCode;
    private TokenUsageTO tokenUsage;
    private String userLogin;
    private String accessToken;
    private BearerTokenTO bearerToken;

    public boolean hasScope(String scope) {
        return Optional.ofNullable(bearerToken)
                .map(t -> t.getScopes().contains(scope))
                .orElse(false);
    }
}
