/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.api.domain.general;

import lombok.Data;

@Data
public class RevertRequestTO {
    private String branchId;
    private long recoveryPointId;
}
