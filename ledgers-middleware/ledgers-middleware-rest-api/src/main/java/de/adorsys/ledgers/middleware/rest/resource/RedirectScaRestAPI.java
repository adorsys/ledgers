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

import de.adorsys.ledgers.middleware.api.domain.sca.GlobalScaResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaLoginOprTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Api(tags = "LDG007 - Redirect SCA", description = "Provide an API to preform SCA process for any kind of banking operation")
public interface RedirectScaRestAPI {
    String BASE_PATH = "/sca";


    @PostMapping("/login")
    @ApiOperation(tags = UnprotectedEndpoint.UNPROTECTED_ENDPOINT, value = "Login For Consent")
    ResponseEntity<GlobalScaResponseTO> authoriseForConsent(@RequestBody ScaLoginOprTO loginOpr);

    @GetMapping(value = "/authorisations/{authorisationId}")
    @ApiOperation(value = "Get SCA", notes = "Get the authorization response object eventually containing the list of selected sca methods.",
            authorizations = @Authorization(value = "apiKey"))
    ResponseEntity<GlobalScaResponseTO> getSCA(@PathVariable("authorisationId") String authorisationId);

    @PutMapping(value = "/authorisations/{authorisationId}/scaMethods/{scaMethodId}")
    @ApiOperation(value = "Select SCA Method", notes = "Select teh given sca method and request for authentication code generation.",
            authorizations = @Authorization(value = "apiKey"))
    ResponseEntity<GlobalScaResponseTO> selectMethod(@PathVariable("authorisationId") String authorisationId,
                                                     @PathVariable("scaMethodId") String scaMethodId);

    @PutMapping(value = "/authorisations/{authorisationId}/authCode")
    @ApiOperation(value = "Validate authorization code", notes = "Validate an authentication code and returns the token", authorizations = @Authorization(value = "apiKey"))
    ResponseEntity<GlobalScaResponseTO> authorize(@PathVariable("authorisationId") String authorisationId,
                                                  @RequestParam(name = "authCode") String authCode);
}
