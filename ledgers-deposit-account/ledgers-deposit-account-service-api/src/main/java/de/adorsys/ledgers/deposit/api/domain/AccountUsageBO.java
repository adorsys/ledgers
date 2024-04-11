/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.deposit.api.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum AccountUsageBO {
    PRIV("PRIV"),
    ORGA("ORGA");

    private static final Map<String, AccountUsageBO> container = new HashMap<>();
    private String value;

    
    AccountUsageBO(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    
    public static Optional<AccountUsageBO> getByValue(String value) {
        return Optional.ofNullable(container.get(value));
    }

    static {
        AccountUsageBO[] var0 = values();

        for (AccountUsageBO usageType : var0) {
            container.put(usageType.getValue(), usageType);
        }
    }
}
