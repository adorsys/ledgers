/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.api.domain.account;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum AccountStatusTO {
    ENABLED("enabled"),
    DELETED("deleted"),
    BLOCKED("blocked");

    private static final Map<String, AccountStatusTO> container = new HashMap<>();

    static {
        for (AccountStatusTO accountStatus : values()) {
            container.put(accountStatus.getValue(), accountStatus);
        }
    }

    private final String value;

    AccountStatusTO(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Optional<AccountStatusTO> getByValue(String value) {
        return Optional.ofNullable(container.get(value));
    }
}
