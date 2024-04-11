/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.token.exchange;

import static java.lang.System.getenv;

/**
 * Configures this API.
 *
 * @author Lorent Lempereur
 */
public class ConfigurationTokenResourceConfiguration {

    private static final String KEYCLOAK_LONG_LIVED_ROLE_NAME = "KEYCLOAK_LONG_LIVED_ROLE_NAME";
    private static final String DEFAULT_KEYCLOAK_LONG_LIVED_ROLE_NAME = "long_lived_token";

    private final String longLivedTokenRole;

    public static ConfigurationTokenResourceConfiguration readFromEnvironment() {
        String longLivedTokenRole = readLongLivedRoleFromEnvironment();
        return new ConfigurationTokenResourceConfiguration(longLivedTokenRole);
    }

    public ConfigurationTokenResourceConfiguration(String longLivedTokenRole) {
        this.longLivedTokenRole = longLivedTokenRole;
    }

    @Override
    public String toString() {
        return "longLivedTokenRole=" + longLivedTokenRole;
    }

    private static String readLongLivedRoleFromEnvironment() {
        String roleForLongLivedTokens = getenv(KEYCLOAK_LONG_LIVED_ROLE_NAME);
        if (roleForLongLivedTokens == null || roleForLongLivedTokens.trim().isEmpty()) {
            return DEFAULT_KEYCLOAK_LONG_LIVED_ROLE_NAME;
        } else {
            return roleForLongLivedTokens;
        }
    }
}
