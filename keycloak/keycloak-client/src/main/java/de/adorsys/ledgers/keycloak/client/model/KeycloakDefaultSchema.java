/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.keycloak.client.model;

import lombok.Data;

import java.util.List;

@Data
public class KeycloakDefaultSchema {
    private final KeycloakRealm realm;
    private final List<String> scopes;
    private final List<String> realmRoles;
    private final KeycloakClient client;

}
