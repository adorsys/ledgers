/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.sca.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthCodeDataBO {
    private String userLogin;
    private String scaUserDataId;
    private String opId;
    private String externalId;
    private String userMessage;
    private int validitySeconds;
    private OpTypeBO opType;
    private String authorisationId;
    private int scaWeight;
}
