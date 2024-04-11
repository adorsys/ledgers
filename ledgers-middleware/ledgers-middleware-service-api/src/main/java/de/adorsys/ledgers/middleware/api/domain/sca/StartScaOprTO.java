/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.api.domain.sca;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StartScaOprTO {
    private String oprId;
    private String externalId;
    private String authorisationId;
    private OpTypeTO opType;

    public StartScaOprTO(String oprId, OpTypeTO opType) {
        this.setOprId(oprId);
        this.setOpType(opType);
    }

    public StartScaOprTO(String oprId, String externalId, String authorizationId, OpTypeTO opType) {
        this.setOprId(oprId);
        this.setExternalId(externalId);
        this.setOpType(opType);
        this.authorisationId = authorizationId;
    }
}
