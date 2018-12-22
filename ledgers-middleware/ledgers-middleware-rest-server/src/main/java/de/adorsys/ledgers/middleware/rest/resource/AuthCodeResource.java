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

import de.adorsys.ledgers.middleware.api.domain.sca.AuthCodeDataTO;
import de.adorsys.ledgers.middleware.api.exception.*;
import de.adorsys.ledgers.middleware.api.service.MiddlewareService;
import de.adorsys.ledgers.middleware.rest.annotation.MiddlewareUserResource;
import de.adorsys.ledgers.middleware.rest.domain.SCAGenerationResponse;
import de.adorsys.ledgers.middleware.rest.domain.SCAValidationRequest;
import de.adorsys.ledgers.middleware.rest.exception.ConflictRestException;
import de.adorsys.ledgers.middleware.rest.exception.NotFoundRestException;
import de.adorsys.ledgers.middleware.rest.exception.ValidationRestException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(AuthCodeResource.AUTH_CODES)
@Api(tags = "SCA" , description= "Provides access to one time password for strong customer authentication.")
@MiddlewareUserResource
public class AuthCodeResource {
    private final static Logger logger = LoggerFactory.getLogger(AuthCodeResource.class);
    static final String AUTH_CODES = "/auth-codes";

    private final MiddlewareService middlewareService;

    public AuthCodeResource(MiddlewareService middlewareService) {
		this.middlewareService = middlewareService;
	}

	@SuppressWarnings("PMD.IdenticalCatchBranches")
    @PostMapping(value = "/generate")
	@ApiOperation(value = "Generate Auth Code", notes="Generate a authetication code for the given data. Requires the user to have successfully logged in.", authorizations =@Authorization(value="apiKey"))
    public SCAGenerationResponse generate(@RequestBody AuthCodeDataTO data) {
        try {
            String opId = middlewareService.generateAuthCode(data);
            logger.debug("Operation id={} was generated for user={}", opId, data.getUserLogin());
            return new SCAGenerationResponse(opId);
        } catch (AuthCodeGenerationMiddlewareException e) {
            logger.error(e.getMessage(), e);
            throw new ValidationRestException(e.getMessage()).withDevMessage(e.getMessage());
        } catch (SCAMethodNotSupportedMiddleException e) {
            logger.error(e.getMessage(), e);
            throw new ConflictRestException(e.getMessage()).withDevMessage(e.getMessage());
        } catch (UserNotFoundMiddlewareException | UserScaDataNotFoundMiddlewareException e) {
            logger.error(e.getMessage());
            throw new NotFoundRestException(e.getMessage()).withDevMessage(e.getMessage());
        }
    }

    @SuppressWarnings("PMD.IdenticalCatchBranches")
    @PostMapping("/{opId}/validate")
	@ApiOperation(value = "Validate Auth Code", notes="Validate an authetication code. Requires the user to have successfully logged in.", authorizations =@Authorization(value="apiKey"))
    public boolean validate(@PathVariable String opId, @RequestBody SCAValidationRequest request) {
        try {
            boolean valid = middlewareService.validateAuthCode(opId, request.getData(), request.getAuthCode());
            logger.debug("The validation of operation with id={} was {}", opId, valid ? "successful" : "failed");
            return valid;
        } catch (SCAOperationValidationMiddlewareException e) {
            logger.error(e.getMessage(), e);
            throw new ValidationRestException(e.getMessage()).withDevMessage(e.getMessage());
        } catch (SCAOperationNotFoundMiddlewareException | PaymentNotFoundMiddlewareException e) {
            logger.error(e.getMessage(), e);
            throw new NotFoundRestException(e.getMessage()).withDevMessage(e.getMessage());
        } catch (SCAOperationUsedOrStolenMiddlewareException | SCAOperationExpiredMiddlewareException e) {
            logger.error(e.getMessage(), e);
            throw new ConflictRestException(e.getMessage()).withDevMessage(e.getMessage());
        }
    }
}
