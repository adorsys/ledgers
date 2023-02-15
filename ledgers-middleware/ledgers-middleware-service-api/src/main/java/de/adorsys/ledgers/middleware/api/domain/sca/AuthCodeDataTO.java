/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.api.domain.sca;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthCodeDataTO {
    private String userLogin;
    private String scaUserDataId;
    private String opId;
    private String opData;
    private String userMessage;
    private int validitySeconds;
    private OpTypeTO opType;
    private String authorisationId;
}
