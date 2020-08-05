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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "LDG007 - Redirect SCA", description = "Provide an API to preform SCA process for any kind of banking operation")
public interface RedirectScaRestAPI {
    String BASE_PATH = "/sca";


    @PostMapping("/login")
    @Operation(tags = UnprotectedEndpoint.UNPROTECTED_ENDPOINT, summary = "Login For Consent")
    ResponseEntity<GlobalScaResponseTO> authoriseForConsent(@RequestBody ScaLoginOprTO loginOpr);

    @GetMapping(value = "/authorisations/{authorisationId}")
    @Operation(summary = "Get SCA", description = "Get the authorization response object eventually containing the list of selected sca methods.")
    @SecurityRequirement(name = "Authorization")
    ResponseEntity<GlobalScaResponseTO> getSCA(@PathVariable("authorisationId") String authorisationId);

    @PutMapping(value = "/authorisations/{authorisationId}/scaMethods/{scaMethodId}")
    @Operation(summary = "Select SCA Method", description = "Select teh given sca method and request for authentication code generation.")
    @SecurityRequirement(name = "Authorization")
    ResponseEntity<GlobalScaResponseTO> selectMethod(@PathVariable("authorisationId") String authorisationId,
                                                     @PathVariable("scaMethodId") String scaMethodId);

    @PutMapping(value = "/authorisations/{authorisationId}/authCode")
    @Operation(summary = "Validate authorization code", description = "Validate an authentication code and returns the token")
    @SecurityRequirement(name = "Authorization")
    ResponseEntity<GlobalScaResponseTO> authorize(@PathVariable("authorisationId") String authorisationId,
                                                  @RequestParam(name = "authCode") String authCode);
}
