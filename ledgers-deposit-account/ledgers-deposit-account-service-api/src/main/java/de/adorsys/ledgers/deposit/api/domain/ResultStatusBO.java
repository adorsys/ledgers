/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.deposit.api.domain;

public enum ResultStatusBO {
    SUCCESS,
    TECHNICAL_FAILURE,
    UNAUTHORIZED_FAILURE,
    LOGICAL_FAILURE,
    NOT_SUPPORTED
}
