/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
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
    @PreAuthorize("@accountAccessSecurityFilter.hasAnyRole('STAFF','CUSTOMER') and @accountAccessSecurityFilter.hasAccessToAccountsWithIbans(#aisConsent.access.listedAccountsIbans)")
    public ResponseEntity<SCAConsentResponseTO> initiateAisConsent(String consentId, AisConsentTO aisConsent) {
        return ResponseEntity.ok(middlewareAccountService.startAisConsent(scaInfoHolder.getScaInfo(), consentId, aisConsent));
    }

    @Override
    @PreAuthorize("@accountAccessSecurityFilter.hasAnyRole('STAFF','CUSTOMER') and @accountAccessSecurityFilter.hasAccessToAccountsWithIbans(#aisConsent.access.listedAccountsIbans)")
    public ResponseEntity<SCAConsentResponseTO> initiatePiisConsent(AisConsentTO aisConsent) {
        return ResponseEntity.ok(middlewareAccountService.startPiisConsent(scaInfoHolder.getScaInfo(), aisConsent));
    }
}
