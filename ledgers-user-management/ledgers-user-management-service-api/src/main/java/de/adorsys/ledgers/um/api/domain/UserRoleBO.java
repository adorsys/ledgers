/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.um.api.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum UserRoleBO {

    CUSTOMER("customer"), // A customer with associated bank accounts
    STAFF("staff"), // a staff member. Can access all accounts
    TECHNICAL("technical"), // a technical user. No SCA
    SYSTEM("system"); // A system user. FOr application management tasks

    private final static Map<String, UserRoleBO> container = new HashMap<>();

    static {
        for (UserRoleBO role : values()) {
            container.put(role.getValue(), role);
        }
    }

    private String value;

    UserRoleBO(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Optional<UserRoleBO> getByValue(String value) {
        return Optional.ofNullable(container.get(value.toLowerCase()));
    }
}


