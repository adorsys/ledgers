/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.api.domain.account;


import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum UsageTypeTO {
    PRIV("PRIV"),
    ORGA("ORGA");

    private static final Map<String, UsageTypeTO> container = new HashMap<>();

    static {
        for (UsageTypeTO usageType : values()) {
            container.put(usageType.getValue(), usageType);
        }
    }

    private final String value;


    UsageTypeTO(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }


    public static Optional<UsageTypeTO> getByValue(String value) {
        return Optional.ofNullable(container.get(value));
    }
}
