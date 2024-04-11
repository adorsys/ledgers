/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.sca.domain.sca.message;

import de.adorsys.ledgers.sca.domain.OpTypeBO;
import lombok.Data;

@Data
public class AppScaMessage extends ScaMessage {
    private String objId;
    private OpTypeBO opType;
    private String authorizationId;
    private int authorizationTTL;
    private String addressedUser;
    private String httpMethod;
    private String confirmationUrl;
    private String authCode;
    private String socketServiceHttpMethod;
    private String socketServicePath;
}
