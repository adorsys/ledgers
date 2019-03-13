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

import de.adorsys.ledgers.middleware.api.domain.sca.OpTypeTO;
import de.adorsys.ledgers.middleware.api.domain.sca.SCALoginResponseTO;
import de.adorsys.ledgers.middleware.api.domain.um.*;
import de.adorsys.ledgers.middleware.api.exception.*;
import de.adorsys.ledgers.middleware.api.service.MiddlewareOnlineBankingService;
import de.adorsys.ledgers.middleware.api.service.MiddlewareUserManagementService;
import de.adorsys.ledgers.middleware.rest.annotation.MiddlewareUserResource;
import de.adorsys.ledgers.middleware.rest.exception.*;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(UserMgmtRestAPI.BASE_PATH)
@MiddlewareUserResource
public class UserMgmtResource implements UserMgmtRestAPI {
    private static final Logger logger = LoggerFactory.getLogger(UserMgmtResource.class);
    private final MiddlewareOnlineBankingService onlineBankingService;
    private final MiddlewareUserManagementService middlewareUserService;
    private final AccessTokenTO accessToken;


    public UserMgmtResource(MiddlewareOnlineBankingService onlineBankingService,
                            MiddlewareUserManagementService middlewareUserService, AccessTokenTO accessToken) {
        super();
        this.onlineBankingService = onlineBankingService;
        this.middlewareUserService = middlewareUserService;
        this.accessToken = accessToken;
    }

    @Override
    public ResponseEntity<UserTO> register(String login, String email, String pin, UserRoleTO role) {
        try {
            // TODO: add activation of non customer members.
            UserTO user = onlineBankingService.register(login, email, pin, role);
            user.setPin(null);
            return ResponseEntity.ok(user);
        } catch (UserAlreadyExistsMiddlewareException e) {
            logger.error(e.getMessage(), e);
            throw new ConflictRestException(e.getMessage()).withDevMessage(e.getMessage());
        }
    }

    /**
     * Authorize returns a bearer token that can be reused by the consuming application.
     *
     * @param login
     * @param pin
     * @return
     */
    @Override
    public ResponseEntity<SCALoginResponseTO> authorise(String login, String pin, UserRoleTO role) {
        try {
            return ResponseEntity.ok(onlineBankingService.authorise(login, pin, role));
        } catch (UserNotFoundMiddlewareException e) {
            logger.error(e.getMessage());
            throw new NotFoundRestException(e.getMessage()).withDevMessage(e.getMessage());
        } catch (InsufficientPermissionMiddlewareException e) {
            logger.error(e.getMessage(), e);
            throw new ForbiddenRestException(e.getMessage()).withDevMessage(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<SCALoginResponseTO> authoriseForConsent(String login, String pin,
                                                                  String consentId, String authorisationId, OpTypeTO opType) {
        try {
            return ResponseEntity.ok(onlineBankingService.authoriseForConsent(login, pin, consentId, authorisationId, opType));
        } catch (UserNotFoundMiddlewareException e) {
            logger.error(e.getMessage(), e);
            throw new NotFoundRestException(e.getMessage()).withDevMessage(e.getMessage());
        } catch (InsufficientPermissionMiddlewareException e) {
            logger.error(e.getMessage(), e);
            throw new ForbiddenRestException(e.getMessage()).withDevMessage(e.getMessage());
        }
    }

    @Override
    @PreAuthorize("loginToken(#scaId,#authorisationId)")
    public ResponseEntity<SCALoginResponseTO> selectMethod(String scaId, String authorisationId, String scaMethodId)
            throws NotFoundRestException, ForbiddenRestException,
                           NotAcceptableRestException, ValidationRestException {
        try {
            return ResponseEntity.ok(onlineBankingService.generateLoginAuthCode(scaMethodId, authorisationId, null, 1800));
        } catch (SCAOperationNotFoundMiddlewareException | UserScaDataNotFoundMiddlewareException e) {
            logger.error(e.getMessage(), e);
            throw new NotFoundRestException(e.getMessage());
        } catch (InsufficientPermissionMiddlewareException e) {
            logger.error(e.getMessage(), e);
            throw new ForbiddenRestException(e.getMessage());
        } catch (SCAMethodNotSupportedMiddleException e) {
            logger.error(e.getMessage(), e);
            throw new NotAcceptableRestException(e.getMessage());
        } catch (SCAOperationValidationMiddlewareException e) {
            logger.error(e.getMessage(), e);
            throw new ValidationRestException(e.getMessage());
        }
    }

    @Override
    @SuppressWarnings("PMD.CyclomaticComplexity")
    @PreAuthorize("loginToken(#scaId,#authorisationId)")
    public ResponseEntity<SCALoginResponseTO> authorizeLogin(String scaId, String authorisationId, String authCode)
            throws GoneRestException, NotFoundRestException, ExpectationFailedRestException,
                           NotAcceptableRestException, ForbiddenRestException {
        try {
            return ResponseEntity.ok(onlineBankingService.authenticateForLogin(authorisationId, authCode));
        } catch (SCAOperationNotFoundMiddlewareException e) {
            logger.error(e.getMessage(), e);
            throw new NotFoundRestException(e.getMessage());
        } catch (SCAOperationValidationMiddlewareException e) {
            logger.error(e.getMessage(), e);
            throw new ExpectationFailedRestException(e.getMessage());
        } catch (SCAOperationExpiredMiddlewareException e) {
            logger.error(e.getMessage(), e);
            throw new GoneRestException(e.getMessage());
        } catch (SCAOperationUsedOrStolenMiddlewareException e) {
            logger.error(e.getMessage(), e);
            throw new NotAcceptableRestException(e.getMessage());
        } catch (InsufficientPermissionMiddlewareException e) {
            logger.error(e.getMessage(), e);
            throw new ForbiddenRestException(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<BearerTokenTO> validate(String token) {
        try {
            BearerTokenTO tokenTO = onlineBankingService.validate(token);
            if (tokenTO != null) {
                return ResponseEntity.ok(tokenTO);
            } else {
                logger.error("Token is null !!!");
                throw new ForbiddenRestException("Token invalid");
            }
        } catch (UserNotFoundMiddlewareException | InsufficientPermissionMiddlewareException e) {
            logger.error(e.getMessage(), e);
            throw new ForbiddenRestException(e.getMessage()).withDevMessage(e.getMessage());
        }

    }

    @Override
    @PreAuthorize("hasAnyRole('STAFF','SYSTEM')")
    public ResponseEntity<UserTO> getUserById(String userId) {
        try {
            return ResponseEntity.ok(middlewareUserService.findById(userId));
        } catch (UserNotFoundMiddlewareException e) {
            logger.error(e.getMessage(), e);
            throw new NotFoundRestException(e.getMessage());
        }
    }

    @Override
    @PreAuthorize("tokenUsage('DIRECT_ACCESS')")
    public ResponseEntity<UserTO> getUser() {
        try {
            return ResponseEntity.ok(middlewareUserService.findById(accessToken.getSub()));
        } catch (UserNotFoundMiddlewareException e) {
            logger.error(e.getMessage(), e);
            throw new NotFoundRestException(e.getMessage());
        }
    }

    @Override
    @PreAuthorize("tokenUsage('DIRECT_ACCESS')")
    public ResponseEntity<Void> updateUserScaData(List<ScaUserDataTO> data) {
        try {
            UserTO userTO = middlewareUserService.findById(accessToken.getSub());
            UserTO user = middlewareUserService.updateScaData(userTO.getLogin(), data);

            URI uri = UriComponentsBuilder.fromUriString(BASE_PATH + "/" + user.getId())
                              .build().toUri();

            return ResponseEntity.created(uri).build();
        } catch (UserNotFoundMiddlewareException e) {
            logger.error(e.getMessage(), e);
            throw new NotFoundRestException(e.getMessage());
        }
    }

    @PutMapping("/{userId}/sca-data")
    @ApiOperation(value = "Updates user SCA", notes = "Updates user authentication methods."
                                                              + "<lu>"
                                                              + "<li>User is implied from the provided access token.</li>"
                                                              + "<li>Actor token (delegation token like ais consent token) can not be used to execute this operation</li>"
                                                              + "</ul>",
            authorizations = @Authorization(value = "apiKey"))
    @ApiResponses(value = {
            @ApiResponse(code = 200, response = Void.class, message = "The user data record without the user pin."),
            @ApiResponse(code = 401, message = "Provided bearer token could not be verified."),
            @ApiResponse(code = 403, message = "Provided bearer token not qualified for this operation."),
    })
    @PreAuthorize("hasAnyRole('STAFF','SYSTEM')")
    public ResponseEntity<Void> updateScaDataByUserId(@PathVariable String userId, @RequestBody List<ScaUserDataTO> data) {
        try {
            UserTO userTO = middlewareUserService.findById(userId);
            UserTO user = middlewareUserService.updateScaData(userTO.getLogin(), data);

            URI uri = UriComponentsBuilder.fromUriString(BASE_PATH + "/" + user.getId())
                              .build().toUri();

            return ResponseEntity.created(uri).build();

        } catch (UserNotFoundMiddlewareException e) {
            logger.error(e.getMessage(), e);
            throw new NotFoundRestException(e.getMessage());
        }
    }

    @Override
    @PreAuthorize("hasAnyRole('STAFF','SYSTEM')")
    public ResponseEntity<List<UserTO>> getAllUsers() {
        return ResponseEntity.ok(middlewareUserService.listUsers(0, 150));
    }
}
