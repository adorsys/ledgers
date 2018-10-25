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

import de.adorsys.ledgers.middleware.domain.SCAOperationTO;
import de.adorsys.ledgers.middleware.domain.ValidationResultTO;
import de.adorsys.ledgers.middleware.exception.NotFoundRestException;
import de.adorsys.ledgers.middleware.exception.ValidationRestException;
import de.adorsys.ledgers.middleware.service.MiddlewareService;
import de.adorsys.ledgers.middleware.service.exception.AuthCodeGenerationMiddlewareException;
import de.adorsys.ledgers.middleware.service.exception.SCAOperationNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.service.exception.SCAOperationValidationMiddlewareException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth-codes")
public class AuthCodeResource {
    private final static Logger logger = LoggerFactory.getLogger(AuthCodeResource.class);

    private final MiddlewareService middlewareService;

    public AuthCodeResource(MiddlewareService middlewareService) {
        this.middlewareService = middlewareService;
    }

    @PostMapping(value = "/{opId}/generate")
    public ResponseEntity generate(@PathVariable String opId, @RequestBody SCAOperationTO operation) {
        try {
            middlewareService.generateAuthCode(opId, operation.getData(), operation.getValiditySeconds());
            return ResponseEntity.noContent().build();
        } catch (AuthCodeGenerationMiddlewareException e) {
            logger.error(e.getMessage(), e);
            throw new ValidationRestException(e.getMessage()).withDevMessage(e.getMessage());
        }
    }

    @SuppressWarnings("PMD.IdenticalCatchBranches")
    @PostMapping("/{opId}/validate")
    public ValidationResultTO validate(@PathVariable String opId, @RequestBody SCAOperationTO operation) {
        try {
            boolean valid = middlewareService.validateAuthCode(opId, operation.getData(), operation.getAuthCode());
            return new ValidationResultTO(valid);
        } catch (SCAOperationValidationMiddlewareException e) {
            logger.error(e.getMessage(), e);
            throw new ValidationRestException(e.getMessage()).withDevMessage(e.getMessage());
        } catch (SCAOperationNotFoundMiddlewareException e) {
            logger.error(e.getMessage(), e);
            throw new NotFoundRestException(e.getMessage()).withDevMessage(e.getMessage());
        }
    }
}
