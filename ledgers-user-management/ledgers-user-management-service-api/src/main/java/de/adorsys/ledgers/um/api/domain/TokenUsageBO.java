/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.um.api.domain;

public enum TokenUsageBO {
    /* Solely used for the auth process. */
    LOGIN,
    /* Used by subject to access resources. */
    DIRECT_ACCESS,
    /* Used by third party to access resources. */
    DELEGATED_ACCESS;
}
