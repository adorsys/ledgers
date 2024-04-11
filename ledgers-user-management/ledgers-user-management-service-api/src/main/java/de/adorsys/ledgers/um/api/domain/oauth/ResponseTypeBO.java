/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.um.api.domain.oauth;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum ResponseTypeBO {
    CODE("code");

    private static final Map<String, ResponseTypeBO> container = new HashMap<>();
    private final String value;

    static {
        for (ResponseTypeBO type : values()) {
            container.put(type.getValue(), type);
        }
    }

    ResponseTypeBO(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public static Optional<ResponseTypeBO> getByValue(String value) {
        return Optional.ofNullable(container.get(value));
    }
}
