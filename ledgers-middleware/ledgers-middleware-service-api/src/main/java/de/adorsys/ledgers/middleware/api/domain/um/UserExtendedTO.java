/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.api.domain.um;

import lombok.Data;

@Data
public class UserExtendedTO extends UserTO {
    private String branchLogin;
}