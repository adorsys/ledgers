package de.adorsys.ledgers.keycloak.client.impl;

import de.adorsys.ledgers.keycloak.client.api.KeycloakDataService;
import de.adorsys.ledgers.keycloak.client.config.KeycloakClientConfig;
import de.adorsys.ledgers.keycloak.client.mapper.KeycloakDataMapper;
import de.adorsys.ledgers.keycloak.client.model.KeycloakClient;
import de.adorsys.ledgers.keycloak.client.model.KeycloakRealm;
import de.adorsys.ledgers.keycloak.client.model.KeycloakUser;
import de.adorsys.ledgers.keycloak.client.model.RequiredAction;
import de.adorsys.ledgers.keycloak.client.rest.KeycloakTokenRestClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakDataServiceImpl implements KeycloakDataService {
    private static final String USER_NOT_FOUND_IN_KEYCLOAK = "User[login: {}] was not found in keycloak.";
    private final Keycloak keycloak;
    private final KeycloakDataMapper mapper;
    private final KeycloakClientConfig configuration;
    private final KeycloakTokenRestClient keycloakTokenRestClient;

    @Value("${local.server.port:8088}")
    private int port;

    @Value("${ledgers.token.lifetime.seconds.login:600}")
    private int loginTokenTTL;
    @Value("${ledgers.token.lifetime.seconds.full:600}")
    private int fullTokenTTL;

    @Override
    public void createDefaultSchema() {
        KeycloakRealm realm = new KeycloakRealm(configuration.getClientRealm(), loginTokenTTL, fullTokenTTL,
                                                configuration.getSmtpServer());
        createRealm(realm);
        createRealmScopes(realm);
        createRealmRoles(realm);
        createClient(realm.getRealm());
    }

    private void createRealm(KeycloakRealm realm) {
        RealmsResource realmsResource = keycloak.realms();
        if (realm.notPresentRealm(realmsResource.findAll())) {
            realmsResource.create(mapper.createRealmRepresentation(realm));
            log.info("Realm [{}] was created", realm.getRealm());
        }
    }

    private void createRealmScopes(KeycloakRealm realm) {
        ClientScopesResource clientScopesResource = keycloak.realm(realm.getRealm()).clientScopes();
        realm.getScopesToAdd(clientScopesResource.findAll())
                .forEach(s -> {
                    clientScopesResource.create(mapper.createClientScopeRepresentation(s));
                    log.info("Client scope [{}] was added to realm [{}]", s, realm.getRealm());
                });
    }

    private void createRealmRoles(KeycloakRealm realm) {
        RolesResource rolesResource = keycloak.realms().realm(realm.getRealm()).roles();
        realm.getRolesToAdd(rolesResource.list())
                .forEach(r -> {
                    rolesResource.create(mapper.createRoleRepresentation(r));
                    log.info("Realm role [{}] was created in realm [{}]", r, realm.getRealm());
                });
    }

    private void createClient(String realm) {
        ClientsResource clientsResource = keycloak.realm(realm).clients();
        KeycloakClient client = new KeycloakClient(configuration, getRedirectUrls());
        if (client.notPresent(clientsResource.findAll())) {
            Response response = clientsResource.create(mapper.createClientRepresentation(client));
            log.info("Client [{}] was created in realm [{}]", client.getClientId(), realm);

            addClientScopes(realm, client, clientsResource, response);
        }
    }

    private void addClientScopes(String realm, KeycloakClient client, ClientsResource clientsResource, Response response) {
        String createdId = CreatedResponseUtil.getCreatedId(response);
        ClientResource clientResource = clientsResource.get(createdId);
        client.getScopes().forEach(s -> {
            String clientScopeId = getClientScopeId(realm, s);
            if (clientScopeId != null) {
                clientResource.addOptionalClientScope(clientScopeId);
                log.info("Client scope [{}] were assigned to client [{}] in realm [{}]", s, client.getClientId(), realm);
            }
        });
    }

    @Override
    public boolean clientExists() {
        try {
            List<ClientRepresentation> byClientId = keycloak.realm(configuration.getClientRealm()).clients().findByClientId(configuration.getExternalClientId());
            return CollectionUtils.isNotEmpty(byClientId);
        } catch (NotFoundException e) {
            return false;
        }
    }

    @Override
    public Optional<KeycloakUser> getUser(String realm, String login) {
        List<UserRepresentation> search = keycloak.realm(realm).users().search(login);
        if (CollectionUtils.isNotEmpty(search)) {
            return Optional.of(mapper.toKeycloakUser(search.get(0)));
        }
        return Optional.empty();
    }

    @Override
    public void createUser(KeycloakUser user) {
        UsersResource usersResource = keycloak.realm(configuration.getClientRealm()).users();
        try (Response response = usersResource.create(mapper.createUserRepresentation(user))) {
            if (HttpStatus.CREATED.value() == response.getStatus()) {
                String userId = CreatedResponseUtil.getCreatedId(response);
                log.info("User[{}] is created with id: {}", user.getLogin(), userId);
                assignUserRoles(configuration.getClientRealm(), userId, user.getRealmRoles());
            }
        }
    }

    @Override
    public void updateUser(KeycloakUser user) {
        UsersResource usersResource = keycloak.realm(configuration.getClientRealm()).users();
        List<UserRepresentation> search = usersResource.search(user.getLogin());
        if (CollectionUtils.isNotEmpty(search)) {
            String userId = search.get(0).getId();
            UserResource userResource = usersResource.get(userId);
            userResource.update(mapper.toUpdateUserPresentation(userResource.toRepresentation(), user));
            log.debug("User[{}] was updated in keycloak.", user.getLogin());

            assignUserRoles(configuration.getClientRealm(), userId, user.getRealmRoles());
            return;
        }
        log.info(USER_NOT_FOUND_IN_KEYCLOAK, user.getLogin());
    }

    @Override
    public void deleteUser(String login) {
        UsersResource usersResource = keycloak.realm(configuration.getClientRealm()).users();
        List<UserRepresentation> search = usersResource.search(login);
        if (CollectionUtils.isNotEmpty(search)) {
            String userId = search.get(0).getId();
            usersResource.delete(userId);
            log.info("User[{}] was deleted from keycloak.", login);
            return;
        }
        log.info(USER_NOT_FOUND_IN_KEYCLOAK, login);
    }

    @Override
    public boolean userExists(String login) {
        List<UserRepresentation> search = keycloak.realm(configuration.getClientRealm()).users().search(login);
        return CollectionUtils.isNotEmpty(search);
    }

    @Override
    public void resetPassword(String login, String password) {
        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(false);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(password);

        UsersResource usersResource = keycloak.realm(configuration.getClientRealm()).users();
        List<UserRepresentation> search = usersResource.search(login);
        if (CollectionUtils.isNotEmpty(search)) {
            String userId = search.get(0).getId();
            UserResource userResource = usersResource.get(userId);
            userResource.resetPassword(passwordCred);

            log.info("User[{}] was password reset.", login);
        }
        log.info(USER_NOT_FOUND_IN_KEYCLOAK, login);
    }

    @Override
    public void resetPasswordViaEmail(String login) {
        Optional<KeycloakUser> user = getUser(configuration.getClientRealm(), login);
        if (user.isPresent()) {
            String accessToken = keycloak.tokenManager().getAccessToken().getToken();
            keycloakTokenRestClient.executeActionsEmail("Bearer " + accessToken,
                                                        user.get().getId(),
                                                        Collections.singletonList(RequiredAction.UPDATE_PASSWORD.name()));

            log.info("User[{}] email for updating password was sent", login);
        }
        log.info(USER_NOT_FOUND_IN_KEYCLOAK, login);
    }

    @Override
    public void assignRealmRoleToUser(String login, List<String> realmRoles) {
        UsersResource usersResource = keycloak.realm(configuration.getClientRealm()).users();
        List<UserRepresentation> search = usersResource.search(login);
        if (CollectionUtils.isNotEmpty(search)) {
            String userId = search.get(0).getId();

            assignUserRoles(configuration.getClientRealm(), userId, realmRoles);
            log.info("Realm roles {} were assigned to User[realm: {}, login: {}] ", realmRoles, configuration.getClientRealm(), login);
            return;
        }
        log.info(USER_NOT_FOUND_IN_KEYCLOAK, login);
    }

    @Override
    public void removeRealmRoleFromUser(String login, List<String> realmRoles) {
        UsersResource usersResource = keycloak.realm(configuration.getClientRealm()).users();
        List<UserRepresentation> search = usersResource.search(login);
        if (CollectionUtils.isNotEmpty(search)) {
            String userId = search.get(0).getId();

            UserResource userResource = usersResource.get(userId);
            realmRoles.forEach(r -> userResource.roles().realmLevel().remove(Collections.singletonList(getRealmRole(configuration.getClientRealm(), r))));
            log.info("Realm roles {} were assigned to User[realm: {}, login: {}] ", realmRoles, configuration.getClientRealm(), login);
            return;
        }
        log.info(USER_NOT_FOUND_IN_KEYCLOAK, login);
    }

    private RoleRepresentation getRealmRole(String realm, String realmRole) {
        return keycloak.realm(realm).roles().get(realmRole).toRepresentation();
    }

    private String getClientScopeId(String realm, String clientScope) {
        List<ClientScopeRepresentation> all = keycloak.realm(realm).clientScopes().findAll();
        for (ClientScopeRepresentation clientScopeRepresentation : all) {
            if (clientScopeRepresentation.getName().equals(clientScope)) {
                return clientScopeRepresentation.getId();
            }
        }
        return null;
    }

    private void assignUserRoles(String realm, String userId, List<String> realmRoles) {
        UserResource userResource = keycloak.realm(realm).users().get(userId);
        realmRoles.forEach(r -> userResource.roles().realmLevel().add(Collections.singletonList(getRealmRole(realm, r))));
    }

    private List<String> getRedirectUrls() {
        List<String> list = new ArrayList<>();
        try {
            String host = InetAddress.getLocalHost().getHostAddress();
            list.add(host + ":" + port);
        } catch (UnknownHostException e) {
            log.error("Could not retrieve host! Fallback to http://localhost:8088 setup!");
        }
        list.add("http://localhost:8088");
        list.add("*");

        return list;
    }
}
