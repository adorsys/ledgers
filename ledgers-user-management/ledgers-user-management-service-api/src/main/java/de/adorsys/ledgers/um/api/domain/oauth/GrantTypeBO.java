package de.adorsys.ledgers.um.api.domain.oauth;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum GrantTypeBO {
    AUTHORISATION_CODE("authorisation_code"),
    REFRESH_TOKEN("refresh_token");

    private static final Map<String, GrantTypeBO> container = new HashMap<>();
    private final String value;

    static {
        for (GrantTypeBO type : values()) {
            container.put(type.getValue(), type);
        }
    }

    GrantTypeBO(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public static Optional<GrantTypeBO> getByValue(String value) {
        return Optional.ofNullable(container.get(value));
    }
}
