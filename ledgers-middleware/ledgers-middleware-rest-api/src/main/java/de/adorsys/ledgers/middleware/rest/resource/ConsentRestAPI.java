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

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import de.adorsys.ledgers.middleware.api.domain.sca.SCAConsentResponseTO;
import de.adorsys.ledgers.middleware.api.domain.um.AisConsentTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.rest.exception.ConflictRestException;
import de.adorsys.ledgers.middleware.rest.exception.NotFoundRestException;
import de.adorsys.ledgers.middleware.rest.exception.ValidationRestException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

@Api(tags = "Consent" , description= "Provide an API to manage consent at the core banking level.")
public interface ConsentRestAPI {
	public static final String BASE_PATH = "/consents";

    /**
     * Initiates an sca process. The result of this initiation is the user's authorization id.
     * 
     * @param scaId : identifies the sca id among the users from which authorization is expected.
     * @param aisConsent : The ais consent target of the authorization. If the authorisation was started
     * by another user, this ais consent must match the one stored in the authorization object.
     * 
     * @return the authorization id. Might send the 
     * @throws ConflictRestException
     */
    @PostMapping(value = "/{consentId}/authorisations")
	@ApiOperation(value = "Start SCA", notes="Starts an authorisation process for establishing account information consent data on the server.", 
		authorizations =@Authorization(value="apiKey"))
    public ResponseEntity<SCAConsentResponseTO> startSCA(@PathVariable("consentId") String consentId, @RequestBody AisConsentTO aisConsent) throws ConflictRestException;
    
    @GetMapping(value = "/{consentId}/authorisations/{authorisationId}")
	@ApiOperation(value = "Get SCA", notes="Get the authorization response object eventually containing the list of selected sca methods.", 
		authorizations =@Authorization(value="apiKey"))
    public ResponseEntity<SCAConsentResponseTO> getSCA(@PathVariable("consentId") String consentId, 
    		@PathVariable("authorisationId") String authorisationId) throws ConflictRestException;
    
    @PutMapping(value = "/{consentId}/authorisations/{authorisationId}/scaMethods/{scaMethodId}")
	@ApiOperation(value = "Select SCA Method", notes="Select teh given sca method and request for authentication code generation.", 
		authorizations =@Authorization(value="apiKey"))
    public ResponseEntity<SCAConsentResponseTO> selectMethod(@PathVariable("consentId") String consentId, 
    		@PathVariable("authorisationId") String authorisationId,
    		@PathVariable("scaMethodId") String scaMethodId) throws ValidationRestException, ConflictRestException, NotFoundRestException;
    
    @PutMapping(value = "/{consentId}/authorisations/{authorisationId}/authCode")
	@ApiOperation(value = "Send an authentication code for validation", notes="Validate an authetication code and returns the cosent token", authorizations =@Authorization(value="apiKey"))
    public ResponseEntity<BearerTokenTO> validate(@PathVariable("consentId") String consentId,
    		@PathVariable("authorisationId") String authorisationId, 
    		@RequestParam(name="authCode") String authCode) throws ValidationRestException,NotFoundRestException, ConflictRestException;
}
