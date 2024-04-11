/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.api.domain.um;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "AccountAccess type", name = "AisAccountAccessType")
public enum AisAccountAccessTypeTO {
    ALL_ACCOUNTS,
    ALL_ACCOUNTS_WITH_BALANCES
}
