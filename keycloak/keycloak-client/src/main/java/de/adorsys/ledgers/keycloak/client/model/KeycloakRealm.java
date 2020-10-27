package de.adorsys.ledgers.keycloak.client.model;

import lombok.Data;
import org.keycloak.representations.idm.ClientScopeRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static de.adorsys.ledgers.middleware.api.domain.Constants.ALL_SCOPES;
import static de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO.ALL_ROLES;

@Data
public class KeycloakRealm {
    private String realm;
    private boolean enabled = true;

    private Integer accessTokenLifespan;
    private Integer accessTokenLifespanForImplicitFlow;
    private Integer clientSessionIdleTimeout;
    private Integer clientSessionMaxLifespan;

    private boolean offlineSessionMaxLifespanEnabled = true;
    private Integer offlineSessionIdleTimeout;
    private Integer offlineSessionMaxLifespan;
    private Integer clientOfflineSessionIdleTimeout;
    private Integer clientOfflineSessionMaxLifespan;

    private boolean registrationAllowed = false;
    private boolean registrationEmailAsUsername = false;

    private boolean verifyEmail = true;
    private boolean loginWithEmailAllowed = true;
    private boolean duplicateEmailsAllowed = false;
    private boolean resetPasswordAllowed = true;
    private boolean editUsernameAllowed = false;

    private Map<String, String> smtpServer;

    public KeycloakRealm(String realmName, Integer loginTokenTTL, Integer offlineTokenTTL, Map<String, String> smtpServer) {
        this.realm = realmName;
        this.accessTokenLifespan = loginTokenTTL;
        this.accessTokenLifespanForImplicitFlow = loginTokenTTL;
        this.clientSessionIdleTimeout = loginTokenTTL;
        this.clientSessionMaxLifespan = loginTokenTTL;

        this.offlineSessionIdleTimeout = offlineTokenTTL;
        this.offlineSessionMaxLifespan = offlineTokenTTL;
        this.clientOfflineSessionIdleTimeout = offlineTokenTTL;
        this.clientOfflineSessionMaxLifespan = offlineTokenTTL;
        this.smtpServer = smtpServer;
    }

    public boolean notPresentRealm(List<RealmRepresentation> allRealms) {
        return allRealms.stream()
                       .map(RealmRepresentation::getRealm)
                       .noneMatch(realm::equals);
    }

    public List<String> getScopesToAdd(List<ClientScopeRepresentation> scopes) {
        List<String> list = scopes.stream()
                                    .map(ClientScopeRepresentation::getName)
                                    .collect(Collectors.toList());
        return filter(list, ALL_SCOPES);
    }

    public List<String> getRolesToAdd(List<RoleRepresentation> roles) {
        List<String> list = roles.stream()
                                    .map(RoleRepresentation::getName)
                                    .collect(Collectors.toList());
        return filter(list, ALL_ROLES);
    }

    private List<String> filter(List<String> filteredList, List<String> streamedSource) {
        return streamedSource.stream()
                       .filter(s -> !filteredList.contains(s))
                       .collect(Collectors.toList());
    }
}
