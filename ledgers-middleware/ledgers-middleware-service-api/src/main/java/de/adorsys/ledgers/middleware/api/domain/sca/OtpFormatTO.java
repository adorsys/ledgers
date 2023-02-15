/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.api.domain.sca;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OtpFormatTO {
    CHARACTERS("characters"),
    INTEGER("integer");

    private final static Map<String, OtpFormatTO> container = new HashMap<>();

    static {
        for (OtpFormatTO otpFormat : values()) {
            container.put(otpFormat.getValue(), otpFormat);
        }
    }

    private String value;

    OtpFormatTO(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonIgnore
    public static Optional<OtpFormatTO> getByValue(String name) {
        return Optional.ofNullable(container.get(name));
    }

    @JsonCreator
    public static OtpFormatTO forValue(String value) {
        return container.get(value);
    }
}
