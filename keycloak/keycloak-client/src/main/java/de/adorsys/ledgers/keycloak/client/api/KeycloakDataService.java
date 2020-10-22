package de.adorsys.ledgers.keycloak.client.api;

import de.adorsys.ledgers.keycloak.client.model.KeycloakUser;

import java.util.List;
import java.util.Optional;

public interface KeycloakDataService {

    void createDefaultSchema();

    boolean clientExists();

    Optional<KeycloakUser> getUser(String realm, String login);

    void createUser(KeycloakUser user);

    void updateUser(KeycloakUser user);

    void deleteUser(String login);

    boolean userExists(String login);

    void resetPassword(String login, String password);

    void resetPasswordViaEmail(String login);

    void assignRealmRoleToUser(String login, List<String> realmRoles);

    void removeRealmRoleFromUser(String login, List<String> realmRoles);
}
