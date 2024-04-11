/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package utils;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.*;

import java.util.*;

public class TestKeycloakService {
    private final Keycloak keycloak;

    public TestKeycloakService(KeycloakContainer keycloakContainer) {
        keycloak = KeycloakBuilder.builder()
                           .serverUrl(keycloakContainer.getAuthServerUrl())
                           .realm("master")
                           .clientId("admin-cli")
                           .username(keycloakContainer.getAdminUsername())
                           .password(keycloakContainer.getAdminPassword())
                           .build();
    }

    public void createRealm(String realm) {
        keycloak.realms().create(createRealmRepresentation(realm));
    }

    public void createClient(String realm, String clientName, String clientSecret) {
        keycloak.realm(realm).clients().create(createClientRepresentation(clientName, clientSecret));
    }

    public void createClientScope(String realm, String clientScope) {
        keycloak.realm(realm).clientScopes().create(createClientScopeRepresentation(clientScope));
    }

    public void createUser(String realm, String login, String password) {
        keycloak.realm(realm).users().create(createUserRepresentation(login, password));
    }

    private ClientRepresentation createClientRepresentation(String clientName, String clientSecret) {
        ClientRepresentation client = new ClientRepresentation();
        client.setId(clientName);
        client.setName(clientName);
        client.setDescription(clientName + " description");
//        client.setPublicClient(true);
        client.setDirectAccessGrantsEnabled(true);
        client.setSecret(clientSecret);
        client.setFullScopeAllowed(true);
        client.setOptionalClientScopes(Arrays.asList("offline_access", "partial_access", "openid"));
        return client;
    }

    private UserRepresentation createUserRepresentation(String login, String password) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(login);
        user.setFirstName(login);
        user.setLastName(login);
        user.setEmail(login + "@email.net");
        user.setEnabled(true);
        user.setClientRoles(new HashMap<>());
        user.setEmailVerified(true);
        user.setCredentials(new ArrayList<>());

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);

        user.getCredentials().add(credential);
        return user;
    }

    private RealmRepresentation createRealmRepresentation(String realm) {
        RealmRepresentation rr = new RealmRepresentation();
        rr.setRealm(realm);
        rr.setDisplayName(realm);
        rr.setEnabled(true);
        return rr;
    }

    private ClientScopeRepresentation createClientScopeRepresentation(String name) {
        ClientScopeRepresentation clientScope = new ClientScopeRepresentation();
        clientScope.setName(name);
        clientScope.setDescription(name + " description");
        clientScope.setProtocol("openid-connect");
        Map<String, String> attributes = new HashMap<>();
        attributes.put("display.on.consent.screen", "true");
        attributes.put("include.in.token.scope", "true");
        clientScope.setAttributes(attributes);
        return clientScope;
    }

}

