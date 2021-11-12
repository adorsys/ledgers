package de.adorsys.ledgers.app.initiation;

import de.adorsys.ledgers.keycloak.client.api.KeycloakDataService;
import de.adorsys.ledgers.keycloak.client.config.KeycloakClientConfig;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.service.MiddlewareUserManagementService;
import de.adorsys.ledgers.middleware.impl.converter.KeycloakUserMapper;
import de.adorsys.ledgers.middleware.impl.converter.UserMapper;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.service.UserService;
import de.adorsys.ledgers.util.exception.UserManagementModuleException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.ws.rs.ProcessingException;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdpConfigurationService {
    private final UserService userService;
    private final MiddlewareUserManagementService middlewareUserService;
    private final KeycloakDataService dataService;
    private final ApplicationContext context;
    private final KeycloakClientConfig keycloakConfig;
    private final KeycloakUserMapper keycloakUserMapper;
    private final UserMapper userMapper;

    @Value("${ledgers.xs2a.funds-confirmation-user-login: admin}")
    private String xs2aAdminLogin;
    @Value("${ledgers.xs2a.funds-confirmation-user-password: admin123}")
    private String xs2aAdminPassword;

    public void configureIDP() {
        boolean clientExists;
        log.info("Keycloak IDP URL is loaded: [{}]", keycloakConfig.getAuthServerUrl());
        try {
            clientExists = dataService.clientExists();
        } catch (ProcessingException e) {
            log.error("Cannot connect to Keycloak IDP on host: [{}]. Ledgers is shutting down.", keycloakConfig.getAuthServerUrl());
            throw e;
        }

        if (!clientExists) {
            log.info("Client does not exist in Keycloak, creating.");
            dataService.createDefaultSchema();
            migrateUsers();
        }
    }

    public void migrateUsers() {
        log.info("Migrating users from Ledgers to Keycloak");

        List<UserBO> users = userService.listUsers(0, Integer.MAX_VALUE);
        users.stream().filter(u -> !u.getLogin().equals(xs2aAdminLogin))
                .forEach(this::createUserInIDP);
        log.info("All users passwords are RESET to 12345 due to migration to new IDP");
    }

    public void createUpdateXs2aAdmin() {
        UserTO admin = new UserTO(xs2aAdminLogin, xs2aAdminLogin + "@example.com", xs2aAdminPassword);
        admin.setUserRoles(Collections.singleton(UserRoleTO.SYSTEM));
        try {
            middlewareUserService.create(admin);
        } catch (UserManagementModuleException e) {
            log.info("Admin exists in Ledgers");
            if (dataService.userExists(xs2aAdminLogin)) {
                middlewareUserService.updatePasswordByLogin(xs2aAdminLogin, xs2aAdminPassword);
            } else {
                createUserInIDP(userMapper.toUserBO(admin));
                log.info("Created admin in IDP");
            }
        }
    }

    private void createUserInIDP(UserBO user) {
        if (!dataService.userExists(user.getLogin())) {
            user.setPin("12345");
            dataService.createUser(keycloakUserMapper.toKeycloakUser(user));
        }
    }
}
