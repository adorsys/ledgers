package de.adorsys.ledgers.middleware.api.domain.oauth;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum GrantTypeTO {
    AUTHORISATION_CODE("authorisation_code"),
    REFRESH_TOKEN("refresh_token");

    private static final Map<String, GrantTypeTO> container = new HashMap<>();
    private final String value;

    static {
        for (GrantTypeTO type : values()) {
            container.put(type.getValue(), type);
        }
    }

    @JsonCreator
    GrantTypeTO(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    @JsonIgnore
    public static Optional<GrantTypeTO> getByValue(String value) {
        return Optional.ofNullable(container.get(value));
    }
}
