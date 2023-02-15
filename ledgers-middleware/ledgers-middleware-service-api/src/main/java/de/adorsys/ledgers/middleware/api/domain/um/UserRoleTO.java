/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.api.domain.um;

import java.util.*;
import java.util.stream.Collectors;

public enum UserRoleTO {

    CUSTOMER("customer"), // A customer with associated bank accounts
    STAFF("staff"), // a staff member. Can access all accounts
    TECHNICAL("technical"), // a technical user. No SCA
    SYSTEM("system"); // A system user. FOr application management tasks

    public static final List<String> ALL_ROLES = Arrays.stream(UserRoleTO.values())
                                                         .map(Enum::name)
                                                         .collect(Collectors.toList());
    private static final Map<String, UserRoleTO> container = new HashMap<>();


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


