/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.api.domain.account;

public enum BalanceTypeTO {
    CLOSING_BOOKED,
    EXPECTED,
    INTERIM_BOOKED,
    OPENING_BOOKED,
    INTERIM_AVAILABLE,
    FORWARD_AVAILABLE,
    NONINVOICED
}
