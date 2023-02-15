/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.impl.converter;

import de.adorsys.ledgers.keycloak.client.model.KeycloakUser;
import de.adorsys.ledgers.um.api.domain.UserBO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class KeycloakUserMapperTest {
    KeycloakUserMapper mapper = new KeycloakUserMapper();

    @Test
    void toKeycloakUser() {
        UserBO bo = new UserBO();
        KeycloakUser result = mapper.toKeycloakUser(bo);
        assertNotNull(result);
    }

    @Test
    void testToKeycloakUser() {
        UserBO bo = new UserBO();
        KeycloakUser result = mapper.toKeycloakUser(bo, "pin");
        assertNotNull(result);
        assertEquals("pin", result.getPassword());

    }
}