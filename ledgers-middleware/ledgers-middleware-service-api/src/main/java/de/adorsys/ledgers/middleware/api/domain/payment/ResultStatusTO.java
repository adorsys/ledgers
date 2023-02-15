/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.api.domain.payment;

public enum ResultStatusTO {
    SUCCESS,
    TECHNICAL_FAILURE,
    UNAUTHORIZED_FAILURE,
    LOGICAL_FAILURE,
    NOT_SUPPORTED
}
