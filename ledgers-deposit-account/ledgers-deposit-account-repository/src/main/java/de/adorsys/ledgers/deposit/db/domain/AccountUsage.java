/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.deposit.db.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;

public enum AccountUsage {
    PRIV("PRIV"),
    ORGA("ORGA");

    private static final Map<String, AccountUsage> container = new HashMap<>();
    private String value;

    @JsonCreator
    AccountUsage(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    @JsonIgnore
    public static Optional<AccountUsage> getByValue(String value) {
        return Optional.ofNullable(container.get(value));
    }

    static {
        AccountUsage[] var0 = values();

        for (AccountUsage usageType : var0) {
            container.put(usageType.getValue(), usageType);
        }
    }
}
