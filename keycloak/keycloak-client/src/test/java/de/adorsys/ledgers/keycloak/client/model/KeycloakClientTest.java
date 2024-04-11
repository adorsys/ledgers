/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.keycloak.client.model;

import de.adorsys.ledgers.keycloak.client.config.KeycloakClientConfig;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.ClientRepresentation;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KeycloakClientTest {

    @Test
    void notPresent() {
        KeycloakClientConfig config = new KeycloakClientConfig();
        config.setExternalClientId("name");
        config.setClientSecret("secret");
        KeycloakClient client = new KeycloakClient(config, List.of("http://localhost:8080"));
        ClientRepresentation representation = new ClientRepresentation();
        representation.setName("name");
        boolean result = client.notPresent(List.of(representation));
        assertFalse(result);
    }

    @Test
    void present() {
        KeycloakClientConfig config = new KeycloakClientConfig();
        config.setExternalClientId("another");
        KeycloakClient client = new KeycloakClient(config, null);
        ClientRepresentation representation = new ClientRepresentation();
        representation.setName("name");
        boolean result = client.notPresent(List.of(representation));
        assertTrue(result);
    }
}