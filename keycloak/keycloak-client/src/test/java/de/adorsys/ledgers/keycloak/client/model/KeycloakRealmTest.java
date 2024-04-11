/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.keycloak.client.model;

import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.RealmRepresentation;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KeycloakRealmTest {

    @Test
    void notPresentRealm() {
        KeycloakRealm realm = new KeycloakRealm("name", 100, 100, null);
        RealmRepresentation representation = new RealmRepresentation();
        representation.setRealm("anotherRealm");
        boolean result = realm.notPresentRealm(List.of(representation));
        assertTrue(result);
    }

    @Test
    void presentRealm() {
        KeycloakRealm realm = new KeycloakRealm("name", 100, 100, null);
        RealmRepresentation representation = new RealmRepresentation();
        representation.setRealm("name");
        boolean result = realm.notPresentRealm(List.of(representation));
        assertFalse(result);
    }
}