/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.rest.resource;

import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareModuleException;
import de.adorsys.ledgers.middleware.api.service.AppManagementService;
import de.adorsys.ledgers.middleware.api.service.MiddlewareUserManagementService;
import de.adorsys.ledgers.middleware.rest.annotation.MiddlewareUserResource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static de.adorsys.ledgers.middleware.api.exception.MiddlewareErrorCode.REQUEST_VALIDATION_FAILURE;

@Slf4j
@RestController
@RequestMapping(AppMgmtRestAPI.BASE_PATH)
@MiddlewareUserResource
@RequiredArgsConstructor
public class AppMgmtResource implements AppMgmtRestAPI {
    private static final String ADMIN_FIRST = "Admin user can not be created after initialization. This must be the first user of the system.";

    private final AppManagementService appManagementService;
    private final MiddlewareUserManagementService userManagementService;

    @Override
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }

    @Override
    @PreAuthorize("hasRole('SYSTEM')")
    public ResponseEntity<Void> initApp() {
        appManagementService.initApp();
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> admin(@RequestBody UserTO adminUser) {
        List<UserTO> users = userManagementService.listUsers(0, 1);
        if (!users.isEmpty()) {
            log.error(ADMIN_FIRST);
            throw MiddlewareModuleException.builder()
                          .errorCode(REQUEST_VALIDATION_FAILURE)
                          .devMsg(ADMIN_FIRST)
                          .build();
        }
        UserTO user = new UserTO();
        user.setLogin(adminUser.getLogin());
        user.setPin(adminUser.getPin());
        user.setEmail(adminUser.getEmail());
        user.getUserRoles().add(UserRoleTO.SYSTEM);
        userManagementService.create(user);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}