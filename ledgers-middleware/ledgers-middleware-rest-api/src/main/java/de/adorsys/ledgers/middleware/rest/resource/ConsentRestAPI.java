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
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import static de.adorsys.ledgers.middleware.rest.utils.Constants.*;

@Tag(name = "LDG005 - Consent", description = "Provide an API to manage consent at the core banking level.")
public interface ConsentRestAPI {
    String BASE_PATH = "/consents";

    @PostMapping(value = "/{consentId}")
    @Operation(summary = "Initiate AIS consent", description = "Validates AIS consent and stores consent initiation to DB")
    @SecurityRequirement(name = API_KEY)
    @SecurityRequirement(name = OAUTH2)
    ResponseEntity<SCAConsentResponseTO> initiateAisConsent(@PathVariable(CONSENT_ID) String consentId, @RequestBody AisConsentTO aisConsent);

    @PostMapping(value = "/piis")
    @Operation(summary = "Generate a consent token for CiF", description = "Generate a consent token for CiF. There is no sca process need as we assume the caller is fully authenticated.")
    @SecurityRequirement(name = API_KEY)
    @SecurityRequirement(name = OAUTH2)
    ResponseEntity<SCAConsentResponseTO> initiatePiisConsent(@RequestBody AisConsentTO piisConsent);
}
