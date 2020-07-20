package de.adorsys.ledgers.middleware.api.domain.um;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum UserRoleTO {

    CUSTOMER("customer"), // A customer with associated bank accounts
    STAFF("staff"), // a staff member. Can access all accounts
    TECHNICAL("technical"), // a technical user. No SCA
    SYSTEM("system"); // A system user. FOr application management tasks

    private final static Map<String, UserRoleTO> container = new HashMap<>();

    static {
        for (UserRoleTO role : values()) {
            container.put(role.getValue(), role);
        }
    }

    private String value;

    UserRoleTO(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Optional<UserRoleTO> getByValue(String value) {
        return Optional.ofNullable(container.get(value.toLowerCase()));
    }
}


