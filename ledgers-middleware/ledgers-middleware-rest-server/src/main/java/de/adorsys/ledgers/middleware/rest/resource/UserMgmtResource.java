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

import java.net.URI;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.exception.InsufficientPermissionMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.UserAlreadyExistsMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.UserNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.service.MiddlewareOnlineBankingService;
import de.adorsys.ledgers.middleware.rest.annotation.MiddlewareUserResource;
import de.adorsys.ledgers.middleware.rest.exception.ConflictRestException;
import de.adorsys.ledgers.middleware.rest.exception.ForbiddenRestException;
import de.adorsys.ledgers.middleware.rest.exception.NotFoundRestException;
import de.adorsys.ledgers.um.api.domain.ScaUserDataBO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.exception.UserNotFoundException;
import de.adorsys.ledgers.um.api.service.UserService;

@RestController
@RequestMapping(UserMgmtRestAPI.BASE_PATH)
@MiddlewareUserResource
public class UserMgmtResource implements UserMgmtRestAPI {

	private static final Logger logger = LoggerFactory.getLogger(UserMgmtResource.class);
    private final MiddlewareOnlineBankingService onlineBankingService;
    private final UserService userService;

    public UserMgmtResource(MiddlewareOnlineBankingService onlineBankingService, UserService userService) {
        this.onlineBankingService = onlineBankingService;
        this.userService = userService;
    }

    @Override
    public ResponseEntity<UserTO> register(@RequestParam(LOGIN_REQUEST_PARAM)String login, 
    		@RequestParam(EMAIL_REQUEST_PARAM) String email, 
    		@RequestParam(PIN_REQUEST_PARAM) String pin,
    		@RequestParam(name=ROLE_REQUEST_PARAM, defaultValue="CUSTOMER") UserRoleTO role) {
    	try {
    		// TODO: add activation of non customer members.
			UserTO user = onlineBankingService.register(login, email, pin, role);
			user.setPin(null);
			return ResponseEntity.ok(user);
		} catch (UserAlreadyExistsMiddlewareException e) {
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
	@SuppressWarnings("PMD.IdenticalCatchBranches")
    public ResponseEntity<BearerTokenTO> authorise(
    		@RequestParam(LOGIN_REQUEST_PARAM)String login, 
    		@RequestParam(PIN_REQUEST_PARAM) String pin, 
    		@RequestParam(ROLE_REQUEST_PARAM) UserRoleTO role){
        try {
            return ResponseEntity.ok(onlineBankingService.authorise(login, pin, role));
        } catch (UserNotFoundMiddlewareException e) {
            throw new NotFoundRestException(e.getMessage()).withDevMessage(e.getMessage());
        } catch (InsufficientPermissionMiddlewareException e) {
            throw new ForbiddenRestException(e.getMessage()).withDevMessage(e.getMessage());    		
		}
    }

    @Override
    public ResponseEntity<BearerTokenTO> validate(@RequestParam("accessToken")String token) {
    	try {
    		BearerTokenTO tokenTO = onlineBankingService.validate(token);
    		if(tokenTO!=null) {
    			return ResponseEntity.ok(tokenTO);
    		} else {
                throw new ForbiddenRestException("Token invalid");    		
    		}
		} catch (UserNotFoundMiddlewareException e) {
            throw new ForbiddenRestException(e.getMessage()).withDevMessage(e.getMessage());    		
		}
    	
    }

    @Override
    public ResponseEntity<UserBO> getUserById(@PathVariable("id") String id) {
        try {
            return ResponseEntity.ok(userService.findById(id));
        } catch (UserNotFoundException e) {
            throw new NotFoundRestException(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<UserBO> getUserByLogin(@RequestParam("login") String login) {
        try {
            return ResponseEntity.ok(userService.findByLogin(login));
        } catch (UserNotFoundException e) {
            throw new NotFoundRestException(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Void> updateUserScaData(@PathVariable String id, @RequestBody List<ScaUserDataBO> data) {
        try {
            UserBO userBO = userService.findById(id);
            UserBO user = userService.updateScaData(data, userBO.getLogin());

            URI uri = UriComponentsBuilder.fromUriString(BASE_PATH + "/" + user.getId())
                    .build().toUri();

            return ResponseEntity.created(uri).build();
        } catch (UserNotFoundException e) {
            throw new NotFoundRestException(e.getMessage());
        }
    }

    // TODO: refactor for user collection pagination
    @Override
    public ResponseEntity<List<UserBO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAll());
    }

}
