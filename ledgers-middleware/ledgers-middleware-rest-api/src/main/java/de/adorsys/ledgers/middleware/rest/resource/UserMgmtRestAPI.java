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

import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.rest.exception.ConflictRestException;
import de.adorsys.ledgers.middleware.rest.exception.ForbiddenRestException;
import de.adorsys.ledgers.middleware.rest.exception.NotFoundRestException;
import de.adorsys.ledgers.um.api.domain.ScaUserDataBO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "User Management" , description= "Provides endpoint for registering, authorizing and managing users.")
public interface UserMgmtRestAPI {
	public static final String BASE_PATH = "/users";
	public static final String REGISTER_PATH = "/register";
	public static final String AUTHORISE_PATH = "/authorise";
	public static final String EMAIL_REQUEST_PARAM = "email";
	public static final String ROLE_REQUEST_PARAM = "role";
	public static final String PIN_REQUEST_PARAM = "pin";
	public static final String LOGIN_REQUEST_PARAM = "login";
    public static final String SCA_DATA_PATH = "/sca-data";

    @PostMapping(REGISTER_PATH)
    @ApiOperation(value="Register User", notes="Registers a user. Registered as a staff member, user will have to be activated.")
    public ResponseEntity<UserTO> register(@RequestParam(LOGIN_REQUEST_PARAM)String login, 
    		@RequestParam(EMAIL_REQUEST_PARAM) String email, 
    		@RequestParam(PIN_REQUEST_PARAM) String pin,
    		@RequestParam(name=ROLE_REQUEST_PARAM, defaultValue="CUSTOMER") UserRoleTO role) throws ConflictRestException;

    /**
     * Authorize returns a bearer token that can be reused by the consuming application.
     * 
     * @param login
     * @param pin
     * @return
     */
    @PostMapping(AUTHORISE_PATH)
    @ApiOperation(value="Authorize User returns Access Token", notes="Authorize any user. But user most specify the target role. return an access token.")
    public ResponseEntity<BearerTokenTO> authorise(
    		@RequestParam(LOGIN_REQUEST_PARAM)String login, 
    		@RequestParam(PIN_REQUEST_PARAM) String pin, 
    		@RequestParam(ROLE_REQUEST_PARAM) UserRoleTO role) throws NotFoundRestException, ForbiddenRestException;

    @PostMapping("/validate")
    @ApiOperation(value="Validate Access Token")
    public ResponseEntity<BearerTokenTO> validate(@RequestParam("accessToken")String token) throws ForbiddenRestException;

    @GetMapping("/{id}")
    @ApiOperation(value="Retrieves User by ID", notes="Retrieves User by ID")
    public ResponseEntity<UserBO> getUserById(@PathVariable("id") String id) throws NotFoundRestException; 

    @GetMapping
    @ApiOperation(value="Retrieves User by login", notes="Retrieves User by login")
    public ResponseEntity<UserBO> getUserByLogin(@RequestParam("login") String login) throws NotFoundRestException;

    @PutMapping("/{id}/" + SCA_DATA_PATH)
    @ApiOperation(value="Updates user SCA", notes="Updates user authentication methods")
    public ResponseEntity<Void> updateUserScaData(@PathVariable("id") String id, @RequestBody List<ScaUserDataBO> data) throws NotFoundRestException;

    // TODO: refactor for user collection pagination
    @GetMapping("/all")
    @ApiOperation(value="Lists users collection", notes="Lists users collection.")
    public ResponseEntity<List<UserBO>> getAllUsers();
}
