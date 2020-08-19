package utils;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.ArrayList;
import java.util.HashMap;

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

}

