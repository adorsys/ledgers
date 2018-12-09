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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.exception.InsufficientPermissionMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.UserAlreadyExistsMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.UserNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.service.MiddlewareOnlineBankingService;
import de.adorsys.ledgers.middleware.rest.exception.ConflictRestException;
import de.adorsys.ledgers.middleware.rest.exception.ForbiddenRestException;
import de.adorsys.ledgers.middleware.rest.exception.NotFoundRestException;

@RestController
@RequestMapping(UserManagementResource.BASE_PATH)
public class UserManagementResource {
	public static final String BASE_PATH = "/users";
	public static final String REGISTER_PATH = "/register";
	public static final String AUTHORISE_PATH = "/authorise";
	public static final String AUTHORISE2_PATH = "/authorise2";
	public static final String EMAIL_REQUEST_PARAM = "email";
	public static final String ROLE_REQUEST_PARAM = "role";
	public static final String PIN_REQUEST_PARAM = "pin";
	public static final String LOGIN_REQUEST_PARAM = "login";
	public static final Logger logger = LoggerFactory.getLogger(UserManagementResource.class);
    private final MiddlewareOnlineBankingService onlineBankingService;

    public UserManagementResource(MiddlewareOnlineBankingService onlineBankingService) {
        this.onlineBankingService = onlineBankingService;
    }

    /**
     * We shall deprecate this. Authorize must return a bearer token.
     * 
     * @param login
     * @param pin
     * @return
     */
    @PostMapping("/authorise")
    @SuppressWarnings("PMD.IdenticalCatchBranches")
    public boolean authorise(@RequestParam(LOGIN_REQUEST_PARAM)String login, @RequestParam(PIN_REQUEST_PARAM) String pin){
        try {
        	return onlineBankingService.authorise(login, pin, UserRoleTO.CUSTOMER)!=null;
        } catch (UserNotFoundMiddlewareException e) {
            logger.error(e.getMessage(), e);
            throw new NotFoundRestException(e.getMessage()).withDevMessage(e.getMessage());
        } catch (InsufficientPermissionMiddlewareException e) {
            logger.error(e.getMessage(), e);
            throw new ForbiddenRestException(e.getMessage()).withDevMessage(e.getMessage());    		
		}
    }

    /**
     * Authorize returns a bearer token that can be reused by the consuming application.
     * 
     * @param login
     * @param pin
     * @return
     */
    @PostMapping(AUTHORISE2_PATH)
	@SuppressWarnings("PMD.IdenticalCatchBranches")
    public String authorise2(@RequestParam(LOGIN_REQUEST_PARAM)String login, @RequestParam(PIN_REQUEST_PARAM) String pin, @RequestParam(ROLE_REQUEST_PARAM) UserRoleTO role){
        try {
            return onlineBankingService.authorise(login, pin, role);
        } catch (UserNotFoundMiddlewareException e) {
            logger.error(e.getMessage(), e);
            throw new NotFoundRestException(e.getMessage()).withDevMessage(e.getMessage());
        } catch (InsufficientPermissionMiddlewareException e) {
            logger.error(e.getMessage(), e);
            throw new ForbiddenRestException(e.getMessage()).withDevMessage(e.getMessage());    		
		}
    }
    
    @PostMapping(REGISTER_PATH)
    public UserTO register(@RequestParam(LOGIN_REQUEST_PARAM)String login, 
    		@RequestParam(EMAIL_REQUEST_PARAM) String email, 
    		@RequestParam(PIN_REQUEST_PARAM) String pin,
    		@RequestParam(name=ROLE_REQUEST_PARAM, defaultValue="CUSTOMER") UserRoleTO role) {
    	try {
			UserTO user = onlineBankingService.register(login, email, pin, role);
			user.setPin(null);
			return user;
		} catch (UserAlreadyExistsMiddlewareException e) {
            logger.error(e.getMessage(), e);
            throw new ConflictRestException(e.getMessage()).withDevMessage(e.getMessage());
		}
    	
    }
}
