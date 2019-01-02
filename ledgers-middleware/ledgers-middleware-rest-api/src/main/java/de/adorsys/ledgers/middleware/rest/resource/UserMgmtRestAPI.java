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

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import de.adorsys.ledgers.middleware.api.domain.sca.SCALoginResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaStatusTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.ScaUserDataTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.rest.exception.ConflictRestException;
import de.adorsys.ledgers.middleware.rest.exception.ExpectationFailedRestException;
import de.adorsys.ledgers.middleware.rest.exception.ForbiddenRestException;
import de.adorsys.ledgers.middleware.rest.exception.GoneRestException;
import de.adorsys.ledgers.middleware.rest.exception.NotAcceptableRestException;
import de.adorsys.ledgers.middleware.rest.exception.NotFoundRestException;
import de.adorsys.ledgers.middleware.rest.exception.ValidationRestException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

@Api(tags = "User Management" , description= "Provides endpoint for registering, authorizing and managing users.")
@SuppressWarnings({"PMD.UnnecessaryModifier"})
public interface UserMgmtRestAPI {
	static final String BASE_PATH = "/users";

    @PostMapping("/register")
    @ApiOperation(value="Register User", notes="Registers a user. Registered as a staff member, user will have to be activated.")
    ResponseEntity<UserTO> register(@RequestParam("login")String login, 
    		@RequestParam("email") String email, 
    		@RequestParam("pin") String pin,
    		@RequestParam(name="role", defaultValue="CUSTOMER") UserRoleTO role) throws ConflictRestException;

    /**
     * Initiates the user login process. Returns a login response object describing how to proceed.
     * 
     * This response object contains an scaId that must be used to proceed with the login.
     * 
     * if the {@link SCALoginResponseTO#getScaStatus()} equals 
     *   	{@link ScaStatusTO#EXEMPTED} the response will contain the final bearer token.
     * 	 	{@link ScaStatusTO#PSUIDENTIFIED} there will be a list of scaMethods for selection in the response.
     * 		{@link ScaStatusTO#SCAMETHODSELECTED} means the auth code has been sent to the user. Must be entered by the user. 
     * 
     * @param login
     * @param pin
     * @param role
     * @return
     * @throws NotFoundRestException
     * @throws ForbiddenRestException
     */
    @PostMapping("/login")
    @ApiOperation(value="Authorize User returns Access Token", 
    	notes="Initiates the user login process. Returns a login response object describing how to proceed. This response object contains an scaId that must be used to proceed with the login."
    			+ "if the ScaStatus is EXEMPTED, the response will contain the final bearer token. If the ScaStatus is PSUIDENTIFIED there will be a list of scaMethods for selection in the response."
    			+ "if ScaStatus is SCAMETHODSELECTED means the auth code has been sent to the user. Must be entered by the user.")
    ResponseEntity<SCALoginResponseTO> authorise(
    		@RequestParam("login")String login, 
    		@RequestParam("pin") String pin, 
    		@RequestParam("role") UserRoleTO role) throws NotFoundRestException, ForbiddenRestException;

    /**
     * Selects the scaMethod to use for sending a login transaction number to the user. This is only valid for the login.
     * 
     * The authorization id is used for the identification an authorization process.
     * 
     * Result must be {@link ScaStatusTO#SCAMETHODSELECTED} means the auth code has been sent to the user. Must be entered by the user.
     *  
     * @param scaId
     * @param authorisationId
     * @param scaMethodId
     * @return
     * @throws ValidationRestException
     * @throws ConflictRestException
     * @throws NotFoundRestException
     */
    @PutMapping(value = "/{scaId}/authorisations/{authorisationId}/scaMethods/{scaMethodId}")
	@ApiOperation(value = "Select SCA Method", notes="Selects the scaMethod to use for sending a login transaction number to the user. This is only valid for the login. "
			+ "The authorization id is used for the identification an authorization process. "
			+ "Result must be ScaStatus is SCAMETHODSELECTED means the auth code has been sent to the user. Must be entered by the user. ", 
		authorizations =@Authorization(value="apiKey"))
    ResponseEntity<SCALoginResponseTO> selectMethod(@PathVariable("scaId") String scaId, 
    		@PathVariable("authorisationId") String authorisationId,
    		@PathVariable("scaMethodId") String scaMethodId) throws ValidationRestException, ConflictRestException, NotFoundRestException, ForbiddenRestException;

    /**
     * Send the auth code for two factor login. The returned response contains a bearer token that can be used 
     * to authenticate further operations.
     * 
     * @param scaId
     * @param authorisationId
     * @param authCode
     * @return
     * @throws GoneRestException
     * @throws NotFoundRestException
     * @throws ConflictRestException
     * @throws ExpectationFailedRestException
     * @throws NotAcceptableRestException
     */
    @PutMapping(value = "/{scaId}/authorisations/{authorisationId}/authCode")
	@ApiOperation(value = "Send an authentication code for validation", 
		notes="Send the auth code for two factor login. The returned response contains a bearer "
				+ "token that can be used to authenticate further operations.", authorizations =@Authorization(value="apiKey"))
    ResponseEntity<SCALoginResponseTO> authorizeLogin(@PathVariable("scaId") String scaId,
    		@PathVariable("authorisationId") String authorisationId, 
    		@RequestParam(name="authCode") String authCode) throws GoneRestException,NotFoundRestException, ConflictRestException, ExpectationFailedRestException, NotAcceptableRestException;
    
    @PostMapping("/validate")
    @ApiOperation(value="Validate Access Token")
    ResponseEntity<BearerTokenTO> validate(@RequestParam("accessToken")String token) throws ForbiddenRestException;

    @GetMapping("/{userId}")
    @ApiOperation(value="Retrieves User by ID", notes="Retrieves User by ID")
    ResponseEntity<UserTO> getUserById(@PathVariable("userId") String userId) throws NotFoundRestException; 

    @GetMapping
    @ApiOperation(value="Retrieves User by login", notes="Retrieves User by login")
    ResponseEntity<UserTO> getUserByLogin(@RequestParam("login") String login) throws NotFoundRestException;

    @PutMapping("/{userId}/sca-data")
    @ApiOperation(value="Updates user SCA", notes="Updates user authentication methods")
    ResponseEntity<Void> updateUserScaData(@PathVariable("userId") String userId, @RequestBody List<ScaUserDataTO> data) throws NotFoundRestException;

    // TODO: refactor for user collection pagination
    @GetMapping("/all")
    @ApiOperation(value="Lists users collection", notes="Lists users collection.")
    ResponseEntity<List<UserTO>> getAllUsers();
}
