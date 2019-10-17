package de.adorsys.ledgers.middleware.api.domain.oauth;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum ResponseTypeTO {
    CODE("code");

    private static final Map<String, ResponseTypeTO> container = new HashMap<>();
    private final String value;

    static {
        for (ResponseTypeTO type : values()) {
            container.put(type.getValue(), type);
        }
    }

    @JsonCreator
    ResponseTypeTO(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    @JsonIgnore
    public static Optional<ResponseTypeTO> getByValue(String value) {
        return Optional.ofNullable(container.get(value));
    }
}
