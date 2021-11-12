package de.adorsys.ledgers.app.initiation;

import de.adorsys.ledgers.app.mock.MockbankInitData;
import de.adorsys.ledgers.keycloak.client.api.KeycloakDataService;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareErrorCode;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareModuleException;
import de.adorsys.ledgers.middleware.api.service.MiddlewareUserManagementService;
import de.adorsys.ledgers.middleware.impl.converter.KeycloakUserMapper;
import de.adorsys.ledgers.middleware.impl.converter.UserMapper;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.service.UserService;
import de.adorsys.ledgers.util.exception.UserManagementModuleException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestDataUserService {
    private final MockbankInitData mockbankInitData;
    private final UserService userService;
    private final MiddlewareUserManagementService middlewareUserService;
    private final KeycloakDataService keycloakDataService;
    private final KeycloakUserMapper keycloakUserMapper;
    private final UserMapper userMapper;

    public void createUsers() {
        log.info("Creating Ledgers test users");

        for (UserTO user : mockbankInitData.getUsers()) {
            String id;
            try {
                id = userService.findByLogin(user.getLogin()).getId();
            } catch (UserManagementModuleException e) {
                user.getUserRoles().add(UserRoleTO.CUSTOMER);
                id = createUser(user);
            }
            user.setId(id);
            if (!keycloakDataService.userExists(user.getLogin())) {
                log.info("Creating test user [{}] in Keycloak", user.getLogin());

                UserBO userBO = userMapper.toUserBO(user);
                keycloakDataService.createUser(keycloakUserMapper.toKeycloakUser(userBO));
            }
        }
    }

    private String createUser(UserTO user) {
        try {
            return middlewareUserService.create(copyUser(user)).getId();
        } catch (UserManagementModuleException e) {
            log.error("User already exists! Should never happen while initiating mock data!");
            throw MiddlewareModuleException.builder().errorCode(MiddlewareErrorCode.NO_SUCH_ALGORITHM).devMsg("Could not create User!").build();
        }
    }

    private UserTO copyUser(UserTO user) {
        UserTO userTO = new UserTO(user.getLogin(), user.getEmail(), user.getPin());
        userTO.setUserRoles(user.getUserRoles());
        userTO.setScaUserData(user.getScaUserData());
        return userTO;
    }
}
