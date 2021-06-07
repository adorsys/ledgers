package de.adorsys.ledgers.keycloak.client.mapper;

import de.adorsys.ledgers.keycloak.client.config.KeycloakClientConfig;
import de.adorsys.ledgers.keycloak.client.model.KeycloakClient;
import de.adorsys.ledgers.keycloak.client.model.KeycloakRealm;
import de.adorsys.ledgers.keycloak.client.model.KeycloakUser;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class KeycloakDataMapperTest {
    private final KeycloakDataMapper mapper = Mappers.getMapper(KeycloakDataMapper.class);

    @Test
    void toKeycloakUser() {
        UserRepresentation representation = getUserRepresentation(true);
        KeycloakUser result = mapper.toKeycloakUser(representation);
        assertEquals(getKeycloakUser(), result);
    }

    @Test
    void toUpdateUserPresentation() {
        UserRepresentation result = mapper.toUpdateUserPresentation(getUserRepresentation(false), getKeycloakUser());
        UserRepresentation expected = getUserRepresentation(true);
        assertEquals(expected.getFirstName(), result.getFirstName());
        assertEquals(expected.getLastName(), result.getLastName());
        assertEquals(expected.getEmail(), result.getEmail());
        assertEquals(expected.isEnabled(), result.isEnabled());
        assertEquals(expected.isEmailVerified(), result.isEmailVerified());
        assertEquals(expected.getRealmRoles(), result.getRealmRoles());
    }

    @Test
    void createRealmRepresentation() {
        RealmRepresentation result = mapper.createRealmRepresentation(new KeycloakRealm("name", 100, 100, null));
        assertNotNull(result);
        assertEquals("name", result.getRealm());
        assertEquals(100, result.getAccessTokenLifespan());
        assertEquals(100, result.getOfflineSessionMaxLifespan());
        assertTrue(result.isEditUsernameAllowed());
    }

    @Test
    void createRoleRepresentation() {
        RoleRepresentation result = mapper.createRoleRepresentation("offline_access");
        assertEquals("offline_access", result.getName());
    }

    @Test
    void createClientRepresentation() {
        KeycloakClientConfig config = new KeycloakClientConfig();
        config.setExternalClientId("id");
        config.setPublicClient(true);

        ClientRepresentation result = mapper.createClientRepresentation(new KeycloakClient(config, List.of("http://test")));
        assertEquals("id", result.getClientId());
        assertEquals("id", result.getName());
        assertEquals(List.of("http://test"), result.getRedirectUris());
        assertEquals(2, result.getWebOrigins().size());
        assertTrue(result.isDirectAccessGrantsEnabled());
        assertTrue(result.isPublicClient());
        assertTrue(result.isFullScopeAllowed());
    }

    @Test
    void createUserRepresentation() {
        UserRepresentation result = mapper.createUserRepresentation(getKeycloakUser());
        assertEquals("userName", result.getUsername());
        assertTrue(result.isEnabled());
        assertTrue(result.isEmailVerified());
        assertEquals("test", result.getFirstName());
        assertEquals("case", result.getLastName());
        assertEquals("test@de.de", result.getEmail());
        assertEquals(1, result.getCredentials().size());
        assertEquals(1, result.getRealmRoles().size());
    }

    @Test
    void createClientScopeRepresentation() {
        ClientScopeRepresentation result = mapper.createClientScopeRepresentation("name");
        assertEquals("name", result.getName());
        assertEquals("openid-connect", result.getProtocol());
        assertEquals(2, result.getAttributes().size());
    }

    private KeycloakUser getKeycloakUser() {
        return new KeycloakUser("id", "userName", null, true, "test", "case", "test@de.de", true, List.of("ADMIN"));
    }

    private UserRepresentation getUserRepresentation(boolean full) {
        UserRepresentation representation = new UserRepresentation();
        if (full) {
            representation.setFirstName("test");
            representation.setLastName("case");
            representation.setEnabled(true);
            representation.setEmailVerified(true);
            representation.setRealmRoles(List.of("ADMIN"));
        }
        representation.setId("id");
        representation.setUsername("userName");
        representation.setEmail("test@de.de");
        return representation;
    }
}
