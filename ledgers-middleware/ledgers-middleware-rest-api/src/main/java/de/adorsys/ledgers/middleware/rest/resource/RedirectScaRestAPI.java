/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.rest.resource;

import de.adorsys.ledgers.middleware.api.domain.sca.GlobalScaResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.StartScaOprTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static de.adorsys.ledgers.middleware.api.domain.Constants.SCOPE_FULL_ACCESS;
import static de.adorsys.ledgers.middleware.api.domain.Constants.SCOPE_SCA;
import static de.adorsys.ledgers.middleware.rest.utils.Constants.*;

@Tag(name = "LDG007 - Redirect SCA", description = "Provide an API to preform SCA process for any kind of banking operation")
public interface RedirectScaRestAPI {
    String BASE_PATH = "/sca";

    @PostMapping("/start")
    @Operation(summary = "Start SCA")
    @SecurityRequirement(name = API_KEY, scopes = {SCOPE_SCA, SCOPE_FULL_ACCESS})
    @SecurityRequirement(name = OAUTH2, scopes = {SCOPE_SCA, SCOPE_FULL_ACCESS})
    ResponseEntity<GlobalScaResponseTO> startSca(@RequestBody StartScaOprTO loginOpr);

    @GetMapping(value = "/authorisations/{authorisationId}")
    @Operation(summary = "Get SCA", description = "Get the authorization response object eventually containing the list of selected sca methods.")
    @SecurityRequirement(name = API_KEY, scopes = {SCOPE_SCA, SCOPE_FULL_ACCESS})
    @SecurityRequirement(name = OAUTH2, scopes = {SCOPE_SCA, SCOPE_FULL_ACCESS})
    ResponseEntity<GlobalScaResponseTO> getSCA(@PathVariable(AUTH_ID) String authorisationId);

    @PutMapping(value = "/authorisations/{authorisationId}/scaMethods/{scaMethodId}")
    @Operation(summary = "Select SCA Method", description = "Select teh given sca method and request for authentication code generation.")
    @SecurityRequirement(name = API_KEY, scopes = {SCOPE_SCA, SCOPE_FULL_ACCESS})
    @SecurityRequirement(name = OAUTH2, scopes = {SCOPE_SCA, SCOPE_FULL_ACCESS})
    ResponseEntity<GlobalScaResponseTO> selectMethod(@PathVariable(AUTH_ID) String authorisationId,
                                                     @PathVariable(SCA_METHOD_ID) String scaMethodId);

    @PutMapping(value = "/authorisations/{authorisationId}/authCode")
    @Operation(summary = "Validate authorization code", description = "Validate an authentication code and returns the token")
    @SecurityRequirement(name = API_KEY, scopes = {SCOPE_SCA, SCOPE_FULL_ACCESS})
    @SecurityRequirement(name = OAUTH2, scopes = {SCOPE_SCA, SCOPE_FULL_ACCESS})
    ResponseEntity<GlobalScaResponseTO> validateScaCode(@PathVariable(AUTH_ID) String authorisationId,
                                                        @RequestParam(name = AUTH_CODE) String authCode);
}
