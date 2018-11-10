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

package de.adorsys.ledgers.middleware.resource;

import de.adorsys.ledgers.middleware.exception.NotFoundRestException;
import de.adorsys.ledgers.middleware.service.MiddlewareService;
import de.adorsys.ledgers.middleware.service.domain.sca.SCAMethodTO;
import de.adorsys.ledgers.middleware.service.exception.UserNotFoundMiddlewareException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ScaMethodResource.SCA_METHODS)
public class ScaMethodResource {
    static final String SCA_METHODS = "/sca-methods";
    private static final Logger logger = LoggerFactory.getLogger(AccountResource.class);

    private final MiddlewareService middlewareService;

    public ScaMethodResource(MiddlewareService middlewareService) {
        this.middlewareService = middlewareService;
    }


    @GetMapping("/{userLogin}")
    public List<SCAMethodTO> getUserScaMethods(@PathVariable String userLogin) {
        try {
            return middlewareService.getSCAMethods(userLogin);
        } catch (UserNotFoundMiddlewareException e) {
            logger.error(e.getMessage(), e);
            throw new NotFoundRestException(e.getMessage()).withDevMessage(e.getMessage());
        }
    }

    @PutMapping("{userLogin}")
    public ResponseEntity updateUserScaMethods(@PathVariable String userLogin, @RequestBody List<SCAMethodTO> methods) {
        try {
            middlewareService.updateScaMethods(methods, userLogin);
        } catch (UserNotFoundMiddlewareException e) {
            logger.error(e.getMessage(), e);
            throw new NotFoundRestException(e.getMessage()).withDevMessage(e.getMessage());
        }
        return ResponseEntity.accepted().build();
    }
}
