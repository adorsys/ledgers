package de.adorsys.ledgers.keycloak.client.impl;

import de.adorsys.ledgers.keycloak.client.config.KeycloakClientConfig;
import de.adorsys.ledgers.keycloak.client.mapper.KeycloakDataMapper;
import de.adorsys.ledgers.keycloak.client.model.KeycloakUser;
import de.adorsys.ledgers.keycloak.client.rest.KeycloakTokenRestClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.*;
import org.keycloak.admin.client.token.TokenManager;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KeycloakDataServiceImplTest {

    @InjectMocks
    private KeycloakDataServiceImpl service;

    @Mock
    private Keycloak keycloak;
    @Mock
    private KeycloakDataMapper mapper;
    @Mock
    private KeycloakClientConfig configuration;
    @Mock
    private KeycloakTokenRestClient keycloakTokenRestClient;

    @Test
    void createDefaultSchema() throws URISyntaxException {
        RealmsResource resource = mock(RealmsResource.class);
        when(keycloak.realms()).thenReturn(resource);
        RealmResource realmResource = mock(RealmResource.class);
        when(keycloak.realm(any())).thenReturn(realmResource);
        ClientScopesResource scopesResource = mock(ClientScopesResource.class);
        when(realmResource.clientScopes()).thenReturn(scopesResource);
        when(resource.realm(any())).thenReturn(realmResource);
        RolesResource rolesResource = mock(RolesResource.class);
        when(realmResource.roles()).thenReturn(rolesResource);
        ClientsResource clientResource = mock(ClientsResource.class);
        when(realmResource.clients()).thenReturn(clientResource);
        when(clientResource.create(any())).thenReturn(Response.created(new URI("")).build());

        service.createDefaultSchema();
        verify(clientResource, times(1)).create(any());
        verify(scopesResource, times(3)).create(any());
        verify(rolesResource, times(4)).create(any());
    }

    @Test
    void clientExists() {
        RealmResource realmResource = mock(RealmResource.class);
        when(keycloak.realm(any())).thenReturn(realmResource);
        ClientsResource clientResource = mock(ClientsResource.class);
        when(realmResource.clients()).thenReturn(clientResource);

        boolean exists = service.clientExists();
        assertFalse(exists);
    }

    @Test
    void clientExists_nf() {
        RealmResource realmResource = mock(RealmResource.class);
        when(keycloak.realm(any())).thenThrow(NotFoundException.class);

        boolean exists = service.clientExists();
        assertFalse(exists);
    }

    @Test
    void getUser() {
        RealmResource realmResource = mock(RealmResource.class);
        when(keycloak.realm(any())).thenReturn(realmResource);
        UsersResource usersResource = mock(UsersResource.class);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.search(any(),eq(true))).thenReturn(List.of(new UserRepresentation()));

        Optional<KeycloakUser> user = service.getUser("testRealm", "login");
        assertTrue(user.isEmpty());
    }

    @Test
    void createUser() throws URISyntaxException {
        RealmResource realmResource = mock(RealmResource.class);
        when(keycloak.realm(any())).thenReturn(realmResource);
        UsersResource usersResource = mock(UsersResource.class);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(any())).thenReturn(Response.created(new URI("")).build());
        UserResource userResource = mock(UserResource.class);
        when(usersResource.get(any())).thenReturn(userResource);

        KeycloakUser user = new KeycloakUser();
        user.setRealmRoles(new ArrayList<>());
        service.createUser(user);
        verify(usersResource, times(1)).create(any());
    }

    @Test
    void updateUser() {
        RealmResource realmResource = mock(RealmResource.class);
        when(keycloak.realm(any())).thenReturn(realmResource);
        UsersResource usersResource = mock(UsersResource.class);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.search(any(),eq(true))).thenReturn(Collections.singletonList(new UserRepresentation()));
        UserResource userResource = mock(UserResource.class);
        when(usersResource.get(any())).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(new UserRepresentation());

        KeycloakUser user = new KeycloakUser();
        user.setRealmRoles(new ArrayList<>());
        service.updateUser(user, user.getLogin());
        verify(userResource, times(1)).update(any());
    }

    @Test
    void deleteUser() {
        RealmResource realmResource = mock(RealmResource.class);
        when(keycloak.realm(any())).thenReturn(realmResource);
        UsersResource usersResource = mock(UsersResource.class);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.search(any(),eq(true))).thenReturn(Collections.singletonList(new UserRepresentation()));

        service.deleteUser("login");
        verify(usersResource, times(1)).delete(any());
    }

    @Test
    void userExists() {
        RealmResource realmResource = mock(RealmResource.class);
        when(keycloak.realm(any())).thenReturn(realmResource);
        UsersResource usersResource = mock(UsersResource.class);
        when(realmResource.users()).thenReturn(usersResource);
        UserResource userResource = mock(UserResource.class);
        when(usersResource.search(any(),eq(true))).thenReturn(Collections.singletonList(new UserRepresentation()));

        boolean exists = service.userExists("login");
        assertTrue(exists);
    }

    @Test
    void resetPassword() {
        RealmResource realmResource = mock(RealmResource.class);
        when(keycloak.realm(any())).thenReturn(realmResource);
        UsersResource usersResource = mock(UsersResource.class);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.search(any(),eq(true))).thenReturn(Collections.singletonList(new UserRepresentation()));
        UserResource userResource = mock(UserResource.class);
        when(usersResource.get(any())).thenReturn(userResource);

        service.resetPassword("login", "password");
        verify(userResource, times(1)).resetPassword(any());
    }

    @Test
    void resetPasswordViaEmail() {
        RealmResource realmResource = mock(RealmResource.class);
        when(keycloak.realm(any())).thenReturn(realmResource);
        UsersResource usersResource = mock(UsersResource.class);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.search(any(), eq(true))).thenReturn(Collections.singletonList(new UserRepresentation()));
        when(mapper.toKeycloakUser(any())).thenReturn(new KeycloakUser());
        TokenManager tokenManager = mock(TokenManager.class);
        when(keycloak.tokenManager()).thenReturn(tokenManager);
        when(tokenManager.getAccessToken()).thenReturn(new AccessTokenResponse());

        service.resetPasswordViaEmail("login");
        verify(keycloakTokenRestClient, times(1)).executeActionsEmail(any(), any(), any());
    }

    @Test
    void assignRealmRoleToUser() {
        RealmResource realmResource = mock(RealmResource.class);
        when(keycloak.realm(any())).thenReturn(realmResource);
        UsersResource usersResource = mock(UsersResource.class);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.search(any(),eq(true))).thenReturn(Collections.singletonList(new UserRepresentation()));
        UserResource userResource = mock(UserResource.class);
        when(usersResource.get(any())).thenReturn(userResource);
        RoleMappingResource roleMappingResource = mock(RoleMappingResource.class);
        when(userResource.roles()).thenReturn(roleMappingResource);
        RoleScopeResource scopeResource = mock(RoleScopeResource.class);
        when(roleMappingResource.realmLevel()).thenReturn(scopeResource);
        RolesResource rolesResource = mock(RolesResource.class);
        when(realmResource.roles()).thenReturn(rolesResource);
        RoleResource roleResource = mock(RoleResource.class);
        when(rolesResource.get(any())).thenReturn(roleResource);

        service.assignRealmRoleToUser("login", Collections.singletonList("role"));
        verify(userResource, times(1)).roles();
    }

    @Test
    void removeRealmRoleFromUser() {
        RealmResource realmResource = mock(RealmResource.class);
        when(keycloak.realm(any())).thenReturn(realmResource);
        UsersResource usersResource = mock(UsersResource.class);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.search(any(),eq(true))).thenReturn(Collections.singletonList(new UserRepresentation()));
        UserResource userResource = mock(UserResource.class);
        when(usersResource.get(any())).thenReturn(userResource);
        RoleMappingResource roleMappingResource = mock(RoleMappingResource.class);
        when(userResource.roles()).thenReturn(roleMappingResource);
        RoleScopeResource scopeResource = mock(RoleScopeResource.class);
        when(roleMappingResource.realmLevel()).thenReturn(scopeResource);
        RolesResource rolesResource = mock(RolesResource.class);
        when(realmResource.roles()).thenReturn(rolesResource);
        RoleResource roleResource = mock(RoleResource.class);
        when(rolesResource.get(any())).thenReturn(roleResource);

        service.removeRealmRoleFromUser("login", Collections.singletonList("role"));
        verify(scopeResource, times(1)).remove(any());
    }
}