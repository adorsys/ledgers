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

import de.adorsys.ledgers.middleware.api.domain.sca.SCAConsentResponseTO;
import de.adorsys.ledgers.middleware.api.domain.um.AisConsentTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "LDG005 - Consent", description = "Provide an API to manage consent at the core banking level.")
public interface ConsentRestAPI {
    String BASE_PATH = "/consents";

    /**
     * Initiates an sca process. The result of this initiation is the user's authorization id.
     *
     * @param consentId  : identifies the sca id among the users from which authorization is expected.
     * @param aisConsent : The ais consent target of the authorization. If the authorisation was started
     *                   by another user, this ais consent must match the one stored in the authorization object.
     * @return the authorization id. Might send the
     */
    // TODO: valid login for customer.
    @PostMapping(value = "/{consentId}/authorisations")
    @Operation(summary = "Start SCA", description = "Starts an authorisation process for establishing account information consent data on the server."/*,
            authorizations = @Authorization(value = "apiKey")*/)
    ResponseEntity<SCAConsentResponseTO> startSCA(@PathVariable("consentId") String consentId, @RequestBody AisConsentTO aisConsent);

    @GetMapping(value = "/{consentId}/authorisations/{authorisationId}")
    @Operation(summary = "Get SCA", description = "Get the authorization response object eventually containing the list of selected sca methods."/*,
            authorizations = @Authorization(value = "apiKey")*/)
    ResponseEntity<SCAConsentResponseTO> getSCA(@PathVariable("consentId") String consentId,
                                                @PathVariable("authorisationId") String authorisationId);

    @PutMapping(value = "/{consentId}/authorisations/{authorisationId}/scaMethods/{scaMethodId}")
    @Operation(summary = "Select SCA Method", description = "Select teh given sca method and request for authentication code generation."/*,
            authorizations = @Authorization(value = "apiKey")*/)
    ResponseEntity<SCAConsentResponseTO> selectMethod(@PathVariable("consentId") String consentId,
                                                      @PathVariable("authorisationId") String authorisationId,
                                                      @PathVariable("scaMethodId") String scaMethodId);

    @PutMapping(value = "/{consentId}/authorisations/{authorisationId}/authCode")
    @Operation(summary = "Send an authentication code for validation", description = "Validate an authetication code and returns the cosent token"/*, authorizations = @Authorization(value = "apiKey")*/)
    ResponseEntity<SCAConsentResponseTO> authorizeConsent(@PathVariable("consentId") String consentId,
                                                          @PathVariable("authorisationId") String authorisationId,
                                                          @RequestParam(name = "authCode") String authCode);


    @PostMapping(value = "/piis")
    @Operation(summary = "Generate a consent token for CiF", description = "Generate a consent token for CiF. There is no sca process need as we assume the caller is fully authenticated."/*,
            authorizations = @Authorization(value = "apiKey")*/)
    ResponseEntity<SCAConsentResponseTO> grantPIISConsent(@RequestBody AisConsentTO piisConsent);
}
