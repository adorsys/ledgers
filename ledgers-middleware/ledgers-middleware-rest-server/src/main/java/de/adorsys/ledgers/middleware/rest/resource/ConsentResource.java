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
import de.adorsys.ledgers.middleware.api.exception.*;
import de.adorsys.ledgers.middleware.api.service.MiddlewareAccountManagementService;
import de.adorsys.ledgers.middleware.rest.annotation.MiddlewareUserResource;
import de.adorsys.ledgers.middleware.rest.exception.*;
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
	public ResponseEntity<SCAConsentResponseTO> startSCA(String consentId, AisConsentTO aisConsent){
		try {
			return ResponseEntity.ok(middlewareAccountService.startSCA(scaInfoHolder.getScaInfo(), consentId, aisConsent));
		} catch (InsufficientPermissionMiddlewareException e) {
			log.error(e.getMessage(), e);
			throw new ForbiddenRestException(e.getMessage()).withDevMessage(e.getMessage());
		}
	}

	// TODO: Bearer token must contain autorization id
	@Override
	public ResponseEntity<SCAConsentResponseTO> getSCA(String consentId, String authorisationId)
			throws ConflictRestException {
		try {
			return ResponseEntity.ok(middlewareAccountService.loadSCAForAisConsent(scaInfoHolder.getUserId(), consentId, authorisationId));
		} catch (SCAOperationExpiredMiddlewareException | AisConsentNotFoundMiddlewareException e) {
            log.error(e.getMessage(), e);
            throw new NotFoundRestException(e.getMessage());
		}
	}

	// TODO: Bearer token must contain autorization id
	@Override
	public ResponseEntity<SCAConsentResponseTO> selectMethod(String consentId, String authorisationId,
			String scaMethodId) throws ValidationRestException, ConflictRestException, NotFoundRestException {
		try {
			return ResponseEntity.ok(middlewareAccountService.selectSCAMethodForAisConsent(scaInfoHolder.getUserId(), consentId, authorisationId, scaMethodId));
		} catch (PaymentNotFoundMiddlewareException | UserScaDataNotFoundMiddlewareException | SCAOperationNotFoundMiddlewareException | AisConsentNotFoundMiddlewareException e) {
            log.error(e.getMessage(), e);
			throw new NotFoundRestException(e.getMessage());
		} catch (SCAOperationValidationMiddlewareException e) {
            log.error(e.getMessage(), e);
			throw new ValidationRestException(e.getMessage());
		} catch (SCAMethodNotSupportedMiddleException e) {
            log.error(e.getMessage(), e);
			throw new NotAcceptableRestException(e.getMessage());
		}
	}

	// TODO: Bearer token must contain autorization id
	@Override
	public ResponseEntity<SCAConsentResponseTO> authorizeConsent(String consentId, String authorisationId, String authCode) {
		try {
			return ResponseEntity.ok(middlewareAccountService.authorizeConsent(scaInfoHolder.getScaInfoWithAuthCode(authCode), consentId));
		} catch (SCAOperationNotFoundMiddlewareException | AisConsentNotFoundMiddlewareException e) {
            log.error(e.getMessage(), e);
			throw new NotFoundRestException(e.getMessage());
		} catch (SCAOperationValidationMiddlewareException e) {
            log.error(e.getMessage(), e);
			throw new ValidationRestException(e.getMessage());
		} catch (SCAOperationUsedOrStolenMiddlewareException e) {
            log.error(e.getMessage(), e);
			throw new NotAcceptableRestException(e.getMessage());
		} catch (SCAOperationExpiredMiddlewareException e) {
            log.error(e.getMessage());
			throw new GoneRestException(e.getMessage());
		}
	}

    @Override
    @PreAuthorize("tokenUsage('DIRECT_ACCESS') and accountInfoFor(#aisConsent)")
    public ResponseEntity<SCAConsentResponseTO> grantPIISConsent(AisConsentTO aisConsent) {
        try {
			return ResponseEntity.ok(middlewareAccountService.grantAisConsent(scaInfoHolder.getScaInfo(), aisConsent));
        } catch (InsufficientPermissionMiddlewareException e) {
            log.error(e.getMessage(), e);
            throw new ForbiddenRestException(e.getMessage()).withDevMessage(e.getMessage());
        } catch (AccountNotFoundMiddlewareException e) {
            log.error(e.getMessage(), e);
            throw new NotFoundRestException(e.getMessage()).withDevMessage(e.getMessage());
		}
    }
}
