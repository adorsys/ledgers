/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.deposit.api.domain;

/**
 * Describes the type of balances a deposit account can carry.
 * 
 * @author fpo
 *
 */
public enum BalanceTypeBO {
    CLOSING_BOOKED,
    EXPECTED,
    INTERIM_BOOKED,
    OPENING_BOOKED,
    INTERIM_AVAILABLE,
    FORWARD_AVAILABLE,
    NONINVOICED
}
