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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.adorsys.ledgers.middleware.api.domain.um.ScaUserDataTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.exception.UserNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.service.MiddlewareUserManagementService;
import de.adorsys.ledgers.middleware.rest.annotation.MiddlewareUserResource;
import de.adorsys.ledgers.middleware.rest.exception.NotFoundRestException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

@RestController
@RequestMapping(ScaMethodResource.SCA_METHODS)
@Api(tags = "SCA Methods" , description= "Provide endpoint for reading and updating user SCA Methods.")
@MiddlewareUserResource
public class ScaMethodResource {
    static final String SCA_METHODS = "/sca-methods";
    private static final Logger logger = LoggerFactory.getLogger(ScaMethodResource.class);

    private final MiddlewareUserManagementService middlewareUserService;

    public ScaMethodResource(MiddlewareUserManagementService middlewareAccountService) {
		this.middlewareUserService = middlewareAccountService;
    }

	@GetMapping("/{userLogin}")
    @ApiOperation(value="Read SCA Methods", notes="Returns user sca methods", authorizations =@Authorization(value="apiKey"))
    public ResponseEntity<List<ScaUserDataTO>> getUserScaMethods(@PathVariable String userLogin) {
        try {
            UserTO user = middlewareUserService.findByUserLogin(userLogin);
            return ResponseEntity.ok(user.getScaUserData());
        } catch (UserNotFoundMiddlewareException e) {
            logger.error(e.getMessage(), e);
            throw new NotFoundRestException(e.getMessage()).withDevMessage(e.getMessage());
        }
    }

    @PutMapping("{userLogin}")
    @ApiOperation(value="Updates SCA Methods", notes="Update the user sca methods", authorizations =@Authorization(value="apiKey"))
    public ResponseEntity<Void> updateUserScaMethods(@PathVariable String userLogin, @RequestBody List<ScaUserDataTO> methods) {
        try {
        	middlewareUserService.updateScaData(userLogin, methods);
        } catch (UserNotFoundMiddlewareException e) {
            logger.error(e.getMessage(), e);
            throw new NotFoundRestException(e.getMessage()).withDevMessage(e.getMessage());
        }
        return ResponseEntity.accepted().build();
    }
}
