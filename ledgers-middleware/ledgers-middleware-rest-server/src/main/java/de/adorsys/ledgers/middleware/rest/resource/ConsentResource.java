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
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.service.MiddlewareAccountManagementService;
import de.adorsys.ledgers.middleware.rest.annotation.MiddlewareUserResource;
import de.adorsys.ledgers.middleware.rest.exception.ConflictRestException;
import de.adorsys.ledgers.middleware.rest.exception.NotFoundRestException;
import de.adorsys.ledgers.middleware.rest.exception.ValidationRestException;

@RestController
@RequestMapping(AccountRestAPI.BASE_PATH)
@SuppressWarnings({"PMD.UnusedPrivateField", "PMD.SingularField"})
@MiddlewareUserResource
public class ConsentResource implements ConsentRestAPI {

	private static final Logger logger = LoggerFactory.getLogger(ConsentResource.class);

    private final MiddlewareAccountManagementService middlewareAccountService;

    public ConsentResource(MiddlewareAccountManagementService middlewareAccountService) {
        this.middlewareAccountService = middlewareAccountService;
    }

	@Override
	public ResponseEntity<SCAConsentResponseTO> startSCA(String consentId, AisConsentTO aisConsent)
			throws ConflictRestException {
		return null;
	}

	@Override
	public ResponseEntity<SCAConsentResponseTO> getSCA(String consentId, String authorisationId)
			throws ConflictRestException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseEntity<SCAConsentResponseTO> selectMethod(String consentId, String authorisationId,
			String scaMethodId) throws ValidationRestException, ConflictRestException, NotFoundRestException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseEntity<BearerTokenTO> validate(String consentId, String authorisationId, String authCode)
			throws ValidationRestException, NotFoundRestException, ConflictRestException {
		// TODO Auto-generated method stub
		return null;
	}

}
