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

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import de.adorsys.ledgers.middleware.api.domain.sca.AuthCodeDataTO;
import de.adorsys.ledgers.middleware.rest.domain.SCAGenerationResponse;
import de.adorsys.ledgers.middleware.rest.domain.SCAValidationRequest;
import de.adorsys.ledgers.middleware.rest.exception.ConflictRestException;
import de.adorsys.ledgers.middleware.rest.exception.NotFoundRestException;
import de.adorsys.ledgers.middleware.rest.exception.ValidationRestException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

@Api(tags = "SCA" , description= "Provides access to one time password for strong customer authentication.")
public interface AuthCodeRestAPI {
    static final String AUTH_CODES = "/auth-codes";

    @PostMapping(value = "/generate")
	@ApiOperation(value = "Generate Auth Code", notes="Generate a authetication code for the given data. Requires the user to have successfully logged in.", authorizations =@Authorization(value="apiKey"))
    public SCAGenerationResponse generate(@RequestBody AuthCodeDataTO data) throws ValidationRestException, ConflictRestException, NotFoundRestException;

    @PostMapping("/{opId}/validate")
	@ApiOperation(value = "Validate Auth Code", notes="Validate an authetication code. Requires the user to have successfully logged in.", authorizations =@Authorization(value="apiKey"))
    public boolean validate(@PathVariable String opId, @RequestBody SCAValidationRequest request) throws ValidationRestException,NotFoundRestException, ConflictRestException;
}
