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

import de.adorsys.ledgers.middleware.api.domain.sca.SCAMethodTO;
import de.adorsys.ledgers.middleware.api.domain.um.ScaUserDataTO;
import de.adorsys.ledgers.middleware.api.exception.UserNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.service.MiddlewareUserManagementService;
import de.adorsys.ledgers.middleware.rest.converter.SCAMethodTOConverter;
import de.adorsys.ledgers.middleware.rest.exception.NotFoundRestException;

@RestController
@RequestMapping(ScaMethodResource.SCA_METHODS)
public class ScaMethodResource {
    static final String SCA_METHODS = "/sca-methods";
    private static final Logger logger = LoggerFactory.getLogger(ScaMethodResource.class);

    private final MiddlewareUserManagementService middlewareUserService;
    private final SCAMethodTOConverter scaMethodTOConverter;

	public ScaMethodResource(MiddlewareUserManagementService middlewareAccountService,
			SCAMethodTOConverter scaMethodTOConverter) {
		super();
		this.middlewareUserService = middlewareAccountService;
		this.scaMethodTOConverter = scaMethodTOConverter;
	}

	@GetMapping("/{userLogin}")
    public List<SCAMethodTO> getUserScaMethods(@PathVariable String userLogin) {
        try {
            return scaMethodTOConverter.toSCAMethodListTO(middlewareUserService.findByUserLogin(userLogin).getScaUserData());
        } catch (UserNotFoundMiddlewareException e) {
            logger.error(e.getMessage(), e);
            throw new NotFoundRestException(e.getMessage()).withDevMessage(e.getMessage());
        }
    }

    @PutMapping("{userLogin}")
    public ResponseEntity updateUserScaMethods(@PathVariable String userLogin, @RequestBody List<SCAMethodTO> methods) {
        try {
        	List<ScaUserDataTO> scaMethodListBO = scaMethodTOConverter.toSCAMethodListBO(methods);
        	middlewareUserService.updateScaData(userLogin, scaMethodListBO);
        } catch (UserNotFoundMiddlewareException e) {
            logger.error(e.getMessage(), e);
            throw new NotFoundRestException(e.getMessage()).withDevMessage(e.getMessage());
        }
        return ResponseEntity.accepted().build();
    }
}
