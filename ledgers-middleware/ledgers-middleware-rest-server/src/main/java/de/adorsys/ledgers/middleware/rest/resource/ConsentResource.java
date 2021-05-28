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
import de.adorsys.ledgers.middleware.api.service.MiddlewareAccountManagementService;
import de.adorsys.ledgers.middleware.rest.annotation.MiddlewareUserResource;
import de.adorsys.ledgers.middleware.rest.security.ScaInfoHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(ConsentRestAPI.BASE_PATH)
@MiddlewareUserResource
public class ConsentResource implements ConsentRestAPI {
    private final ScaInfoHolder scaInfoHolder;
    private final MiddlewareAccountManagementService middlewareAccountService;

    @Override
    @PreAuthorize("hasAnyRole('STAFF','CUSTOMER') and hasAccessToAccountsWithIbans(#aisConsent.access.listedAccountsIbans)")
    public ResponseEntity<SCAConsentResponseTO> initiateAisConsent(String consentId, AisConsentTO aisConsent) {
        return ResponseEntity.ok(middlewareAccountService.startAisConsent(scaInfoHolder.getScaInfo(), consentId, aisConsent));
    }

    @Override
    @PreAuthorize("hasAnyRole('STAFF','CUSTOMER') and hasAccessToAccountsWithIbans(#aisConsent.access.listedAccountsIbans)")
    public ResponseEntity<SCAConsentResponseTO> initiatePiisConsent(AisConsentTO aisConsent) {
        return ResponseEntity.ok(middlewareAccountService.startPiisConsent(scaInfoHolder.getScaInfo(), aisConsent));
    }
}
