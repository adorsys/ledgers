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
import de.adorsys.ledgers.middleware.api.domain.sca.StartScaOprTO;
import de.adorsys.ledgers.middleware.api.service.MiddlewareRedirectScaService;
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
@RequestMapping(RedirectScaRestAPI.BASE_PATH)
@MiddlewareUserResource
public class RedirectScaResource implements RedirectScaRestAPI {
    private final ScaInfoHolder scaInfoHolder;
    private final MiddlewareRedirectScaService scaService;

    @Override
    @PreAuthorize("hasScaScope() and hasAccessToAccountByScaOperation(#startScaOpr)")
    public ResponseEntity<GlobalScaResponseTO> startSca(StartScaOprTO startScaOpr) {
        return ResponseEntity.ok(scaService.startScaOperation(startScaOpr, scaInfoHolder.getScaInfo()));
    }

    @Override
    @PreAuthorize("hasScaScope() and hasAccessToAccountByAuthorizationId(#authorisationId)")
    public ResponseEntity<GlobalScaResponseTO> getSCA(String authorisationId) {
        return ResponseEntity.ok(scaService.getMethods(authorisationId, scaInfoHolder.getScaInfo()));
    }

    @Override
    @PreAuthorize("hasScaScope() and hasAccessToAccountByAuthorizationId(#authorisationId)")
    public ResponseEntity<GlobalScaResponseTO> selectMethod(String authorisationId, String scaMethodId) {
        return ResponseEntity.ok(scaService.selectMethod(scaInfoHolder.getScaInfoWithScaMethodIdAndAuthorisationId(scaMethodId, authorisationId)));
    }

    @Override
    @PreAuthorize("hasScaScope() and hasAccessToAccountByAuthorizationId(#authorisationId)")
    public ResponseEntity<GlobalScaResponseTO> validateScaCode(String authorisationId, String authCode) {
        return ResponseEntity.ok(scaService.confirmAuthorization(scaInfoHolder.getScaInfoWithAuthCodeAndAuthorisationId(authCode, authorisationId)));
    }
}
