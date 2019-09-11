/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.ledgers.middleware.rest.resource;

import de.adorsys.ledgers.middleware.api.domain.sca.SCALoginResponseTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareModuleException;
import de.adorsys.ledgers.middleware.api.service.AppManagementService;
import de.adorsys.ledgers.middleware.api.service.MiddlewareOnlineBankingService;
import de.adorsys.ledgers.middleware.api.service.MiddlewareUserManagementService;
import de.adorsys.ledgers.middleware.rest.annotation.MiddlewareUserResource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final MiddlewareOnlineBankingService middlewareUserService;

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
    public ResponseEntity<BearerTokenTO> admin(@RequestBody(required = true) UserTO adminUser) {
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

        SCALoginResponseTO scaLoginResponseTO = middlewareUserService.authorise(adminUser.getLogin(), adminUser.getPin(), UserRoleTO.SYSTEM);
        return ResponseEntity.ok(scaLoginResponseTO.getBearerToken());
    }
}
