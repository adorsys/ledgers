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

import de.adorsys.ledgers.middleware.api.domain.account.AccountReferenceTO;
import de.adorsys.ledgers.middleware.api.domain.sca.AuthConfirmationTO;
import de.adorsys.ledgers.middleware.api.domain.sca.OpTypeTO;
import de.adorsys.ledgers.middleware.api.domain.sca.SCALoginResponseTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.ScaUserDataTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareModuleException;
import de.adorsys.ledgers.middleware.api.service.MiddlewareAuthConfirmationService;
import de.adorsys.ledgers.middleware.api.service.MiddlewareOnlineBankingService;
import de.adorsys.ledgers.middleware.api.service.MiddlewareUserManagementService;
import de.adorsys.ledgers.middleware.rest.annotation.MiddlewareUserResource;
import de.adorsys.ledgers.middleware.rest.security.ScaInfoHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

import static de.adorsys.ledgers.middleware.api.exception.MiddlewareErrorCode.AUTHENTICATION_FAILURE;

//import io.swagger.annotations.ApiOperation;
//import io.swagger.annotations.ApiResponse;
//import io.swagger.annotations.ApiResponses;
//import io.swagger.annotations.Authorization;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(UserMgmtRestAPI.BASE_PATH)
@MiddlewareUserResource
public class UserMgmtResource implements UserMgmtRestAPI {
    private final MiddlewareOnlineBankingService onlineBankingService;
    private final MiddlewareUserManagementService middlewareUserService;
    private final MiddlewareAuthConfirmationService authConfirmationService;
    private final ScaInfoHolder scaInfoHolder;

    @Override
    public ResponseEntity<Boolean> multilevel(String login, String iban) {
        return ResponseEntity.ok(middlewareUserService.checkMultilevelScaRequired(login, iban));
    }

    @Override
    public ResponseEntity<Boolean> multilevelAccounts(String login, List<AccountReferenceTO> references) {
        return ResponseEntity.ok(middlewareUserService.checkMultilevelScaRequired(login, references));
    }

    @Override
    public ResponseEntity<UserTO> register(String login, String email, String pin, UserRoleTO role) {
        UserTO user = onlineBankingService.register(login, email, pin, role);
        user.setPin(null);
        return ResponseEntity.ok(user);
    }

    @Override
    public ResponseEntity<SCALoginResponseTO> authorise(String login, String pin, UserRoleTO role) {
        return ResponseEntity.ok(onlineBankingService.authorise(login, pin, role));
    }

    @Override
    public ResponseEntity<SCALoginResponseTO> authoriseForConsent(String login, String pin,
                                                                  String consentId, String authorisationId, OpTypeTO opType) {
        return ResponseEntity.ok(onlineBankingService.authoriseForConsent(login, pin, consentId, authorisationId, opType));
    }

    @Override
    public ResponseEntity<SCALoginResponseTO> authoriseForConsent(String consentId, String authorisationId, OpTypeTO opType) {
        return ResponseEntity.ok(onlineBankingService.authoriseForConsentWithToken(scaInfoHolder.getScaInfo(), consentId, authorisationId, opType));
    }

    @Override
    @PreAuthorize("loginToken(#scaId,#authorisationId)")
    public ResponseEntity<SCALoginResponseTO> selectMethod(String scaId, String authorisationId, String scaMethodId) {
        return ResponseEntity.ok(onlineBankingService.generateLoginAuthCode(scaInfoHolder.getScaInfoWithScaMethodIdAndAuthorisationId(scaMethodId, authorisationId), null, 1800));
    }

    @Override
    @PreAuthorize("loginToken(#scaId,#authorisationId)")
    public ResponseEntity<SCALoginResponseTO> authorizeLogin(String scaId, String authorisationId, String authCode) {
        return ResponseEntity.ok(onlineBankingService.authenticateForLogin(scaInfoHolder.getScaInfoWithAuthCode(authCode)));
    }

    @Override
    public ResponseEntity<BearerTokenTO> validate(String token) {
        BearerTokenTO tokenTO = onlineBankingService.validate(token);
        if (tokenTO != null) {
            return ResponseEntity.ok(tokenTO);
        } else {
            log.error("Token is null !!!");
            throw MiddlewareModuleException.builder()
                          .errorCode(AUTHENTICATION_FAILURE)
                          .devMsg("Token invalid")
                          .build();
        }
    }

    @Override
    @PreAuthorize("hasAnyRole('STAFF','SYSTEM')")
    public ResponseEntity<UserTO> getUserById(String userId) {
        return ResponseEntity.ok(middlewareUserService.findById(userId));
    }

    @Override
    @PreAuthorize("tokenUsage('DIRECT_ACCESS')")
    public ResponseEntity<UserTO> getUser() {
        return ResponseEntity.ok(middlewareUserService.findById(scaInfoHolder.getUserId()));
    }

    @Override
    @PreAuthorize("tokenUsage('DIRECT_ACCESS')")
    public ResponseEntity<Void> editSelf(UserTO user) {
        middlewareUserService.editBasicSelf(scaInfoHolder.getUserId(), user);
        return ResponseEntity.accepted().build();
    }

    @Override
    @PreAuthorize("tokenUsage('DIRECT_ACCESS')")
    public ResponseEntity<Void> updateUserScaData(List<ScaUserDataTO> data) {
        UserTO userTO = middlewareUserService.findById(scaInfoHolder.getUserId());
        UserTO user = middlewareUserService.updateScaData(userTO.getLogin(), data);

        URI uri = UriComponentsBuilder.fromUriString(BASE_PATH + "/" + user.getId())
                          .build().toUri();

        return ResponseEntity.created(uri).build();
    }

    @Override
    @PreAuthorize("hasAnyRole('STAFF','SYSTEM')")
    public ResponseEntity<Void> updateScaDataByUserId(String userId, List<ScaUserDataTO> data) {
        UserTO userTO = middlewareUserService.findById(userId);
        UserTO user = middlewareUserService.updateScaData(userTO.getLogin(), data);

        URI uri = UriComponentsBuilder.fromUriString(BASE_PATH + "/" + user.getId())
                          .build().toUri();

        return ResponseEntity.created(uri).build();
    }

    @Override
    @PreAuthorize("hasAnyRole('STAFF','SYSTEM')")
    public ResponseEntity<List<UserTO>> getAllUsers() {
        return ResponseEntity.ok(middlewareUserService.listUsers(0, 1000));
    }

    @Override
    @PreAuthorize("tokenUsages('DIRECT_ACCESS','DELEGATED_ACCESS')")
    public ResponseEntity<AuthConfirmationTO> verifyAuthConfirmationCode(String authorisationId, String authConfirmCode) {
        return ResponseEntity.ok(authConfirmationService.verifyAuthConfirmationCode(authorisationId, authConfirmCode, scaInfoHolder.getScaInfo().getUserLogin()));
    }

    @Override
    @PreAuthorize("tokenUsages('DIRECT_ACCESS','DELEGATED_ACCESS')")
    public ResponseEntity<AuthConfirmationTO> completeAuthConfirmation(String authorisationId, boolean authCodeConfirmed) {
        return ResponseEntity.ok(authConfirmationService.completeAuthConfirmation(authorisationId, authCodeConfirmed, scaInfoHolder.getScaInfo().getUserLogin()));
    }
}
