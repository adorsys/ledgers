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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.adorsys.ledgers.middleware.api.domain.sca.SCAConsentResponseTO;
import de.adorsys.ledgers.middleware.api.domain.um.AisConsentTO;
import de.adorsys.ledgers.middleware.api.exception.AisConsentNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.InsufficientPermissionMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.PaymentNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.SCAMethodNotSupportedMiddleException;
import de.adorsys.ledgers.middleware.api.exception.SCAOperationExpiredMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.SCAOperationNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.SCAOperationUsedOrStolenMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.SCAOperationValidationMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.UserScaDataNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.service.MiddlewareAccountManagementService;
import de.adorsys.ledgers.middleware.rest.annotation.MiddlewareUserResource;
import de.adorsys.ledgers.middleware.rest.exception.ConflictRestException;
import de.adorsys.ledgers.middleware.rest.exception.ForbiddenRestException;
import de.adorsys.ledgers.middleware.rest.exception.GoneRestException;
import de.adorsys.ledgers.middleware.rest.exception.NotAcceptableRestException;
import de.adorsys.ledgers.middleware.rest.exception.NotFoundRestException;
import de.adorsys.ledgers.middleware.rest.exception.ValidationRestException;

@RestController
@RequestMapping(ConsentRestAPI.BASE_PATH)
@MiddlewareUserResource
public class ConsentResource implements ConsentRestAPI {
	private static final Logger logger = LoggerFactory.getLogger(ConsentResource.class);

    private final MiddlewareAccountManagementService middlewareAccountService;

    public ConsentResource(MiddlewareAccountManagementService middlewareAccountService) {
        this.middlewareAccountService = middlewareAccountService;
    }

	@Override
	public ResponseEntity<SCAConsentResponseTO> startSCA(String consentId, AisConsentTO aisConsent){
		try {
			return ResponseEntity.ok(middlewareAccountService.startSCA(consentId, aisConsent));
		} catch (InsufficientPermissionMiddlewareException e) {
			logger.error(e.getMessage(), e);
			throw new ForbiddenRestException(e.getMessage()).withDevMessage(e.getMessage());
		}
	}

	@Override
	public ResponseEntity<SCAConsentResponseTO> getSCA(String consentId, String authorisationId)
			throws ConflictRestException {
		try {
			return ResponseEntity.ok(middlewareAccountService.loadSCAForAisConsent(consentId, authorisationId));
		} catch (SCAOperationExpiredMiddlewareException | AisConsentNotFoundMiddlewareException e) {
            logger.error(e.getMessage(), e);
            throw new NotFoundRestException(e.getMessage());
		}
	}

	@Override
	public ResponseEntity<SCAConsentResponseTO> selectMethod(String consentId, String authorisationId,
			String scaMethodId) throws ValidationRestException, ConflictRestException, NotFoundRestException {
		try {
			return ResponseEntity.ok(middlewareAccountService.selectSCAMethodForAisConsent(consentId, authorisationId, scaMethodId));
		} catch (PaymentNotFoundMiddlewareException | UserScaDataNotFoundMiddlewareException | SCAOperationNotFoundMiddlewareException | AisConsentNotFoundMiddlewareException e) {
            logger.error(e.getMessage(), e);
			throw new NotFoundRestException(e.getMessage());
		} catch (SCAOperationValidationMiddlewareException e) {
            logger.error(e.getMessage(), e);
			throw new ValidationRestException(e.getMessage());
		} catch (SCAMethodNotSupportedMiddleException e) {
            logger.error(e.getMessage(), e);
			throw new NotAcceptableRestException(e.getMessage());
		}
	}

	@Override
	public ResponseEntity<SCAConsentResponseTO> authorizeConsent(String consentId, String authorisationId, String authCode)
			throws ValidationRestException, NotFoundRestException, GoneRestException {
		try {
			return ResponseEntity.ok(middlewareAccountService.authorizeConsent(consentId, authorisationId, authCode));
		} catch (SCAOperationNotFoundMiddlewareException | AisConsentNotFoundMiddlewareException e) {
            logger.error(e.getMessage(), e);
			throw new NotFoundRestException(e.getMessage());
		} catch (SCAOperationValidationMiddlewareException e) {
            logger.error(e.getMessage(), e);
			throw new ValidationRestException(e.getMessage());
		} catch (SCAOperationUsedOrStolenMiddlewareException e) {
            logger.error(e.getMessage(), e);
			throw new NotAcceptableRestException(e.getMessage());
		} catch (SCAOperationExpiredMiddlewareException e) {
            logger.error(e.getMessage());
			throw new GoneRestException(e.getMessage());
		}
	}

}
