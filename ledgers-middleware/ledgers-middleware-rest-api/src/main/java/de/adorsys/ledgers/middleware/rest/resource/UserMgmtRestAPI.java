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
import de.adorsys.ledgers.middleware.api.domain.sca.OpTypeTO;
import de.adorsys.ledgers.middleware.api.domain.sca.SCALoginResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaStatusTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.ScaUserDataTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import io.swagger.annotations.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "LDG001 - User Management", description = "Provides endpoint for registering, authorizing and managing users.")
public interface UserMgmtRestAPI {
    String BASE_PATH = "/users";

    //==========================================================================================================================
    //
    //	SELF SERVICE OPERATIONS. NO CREDENTIAL REQUIRED
    //
    //==========================================================================================================================

    @GetMapping("/multilevel")
    @ApiOperation(tags = UnprotectedEndpoint.UNPROTECTED_ENDPOINT, value = "Check if multilevel SCA required for certain user")
    @ApiResponses(value = {
            @ApiResponse(code = 200, response = boolean.class, message = "Boolean representation of requirement for multi-level sca")
    })
    ResponseEntity<Boolean> multilevel(@RequestParam("login") String login, @RequestParam("iban") String iban);

    @PostMapping("/multilevel")
    @ApiOperation(tags = UnprotectedEndpoint.UNPROTECTED_ENDPOINT, value = "Check if multilevel SCA required for certain user")
    @ApiResponses(value = {
            @ApiResponse(code = 200, response = boolean.class, message = "Boolean representation of requirement for multi-level sca")
    })
    ResponseEntity<Boolean> multilevelAccounts(@RequestParam("login") String login, @RequestBody List<AccountReferenceTO> references);

    /**
     * Registers a new user with the system. Activation is dependent on the user role.
     * - A Customer will be automatically activated.
     * - For all other roles, explicite activation is required.
     *
     * @param login user login
     * @param email users e-mail address
     * @param pin   users pin
     * @param role  users Role at Ledgers
     * @return User object with erased pin
     */
    @PostMapping("/register")
    @ApiOperation(tags = UnprotectedEndpoint.UNPROTECTED_ENDPOINT, value = "Register", notes = "Registers a user."
                                                                                                       + "<ul>"
                                                                                                       + "<li>A user is always registered as customer and is activated by default.</li>"
                                                                                                       + "<li>A user can only be given another role by an administrating STAFF member.</li>"
                                                                                                       + "</ul>")
    @ApiResponses(value = {
            @ApiResponse(code = 200, response = UserTO.class, message = "The user data record without the user pin."),
            @ApiResponse(code = 409, message = "Conflict. A user with email or login name already exist.")
    })
    ResponseEntity<UserTO> register(@RequestParam("login") String login,
                                    @RequestParam("email") String email,
                                    @RequestParam("pin") String pin,
                                    // TODO remove role parameter.
                                    @RequestParam(name = "role", defaultValue = "CUSTOMER") UserRoleTO role);


    /**
     * Initiates the user login process. Returns a login response object describing how to proceed.
     * <p>
     * This response object contains an scaId that must be used to proceed with the login.
     * <p>
     * if the {@link SCALoginResponseTO#getScaStatus()} equals
     * {@link ScaStatusTO#EXEMPTED} the response will contain the final bearer token.
     * {@link ScaStatusTO#SCAMETHODSELECTED} means the auth code has been sent to the user. Must be entered by the user.
     * {@link ScaStatusTO#PSUAUTHENTICATED} there will be a list of scaMethods for selection in the response.
     * {@link ScaStatusTO#PSUIDENTIFIED} the user exists but given password/pin did not match.
     *
     * @param login user login
     * @param pin   users pin
     * @param role  users Role at Ledgers
     * @return Login response containing a bearer token in case of success with some additional info if further SCA is required
     */
    @PostMapping("/login")
    @ApiOperation(tags = UnprotectedEndpoint.UNPROTECTED_ENDPOINT, value = "Login",
            notes = "Initiates the user login process. Returns a login response object describing how to proceed. "
                            + "This response object contains both an scaId and an authorizationId that must be used to identify this login process.<br/>"
                            + "This response also contains an scaStatus that indicates the next stept to take."
                            + "<ul>"
                            + "<li>EXEMPTED: the response will contain the final bearer token."
                            + "<ul>"
                            + "<li>The login process is complete in this single step.</li>"
                            + "<li>The response contains a full JWT access token that can be used to access account and payment endpoints.</li>"
                            + "/ul>"
                            + "</li>"
                            + "<li>SCAMETHODSELECTED: the auth code has been directly sent to the user because the user has only one sca method configured for login. "
                            + "<ul>"
                            + "<li>Auth code Must be entered by the user.</li>"
                            + "<li>Response contains a JWT token that must be used to authenticate for further action. This token can not be used to perform account access because the authentication process is not completed.<li>"
                            + "<li>Caller must proceed with the authCode endpoint: /{scaId}/authorisations/{authorisationId}/authCode</li>"
                            + "</ul>"
                            + "</li>"
                            + "<li>PSUAUTHENTICATED: the user has many sca methods configured for login."
                            + "<ul>"
                            + "<li>The response contains a list of scaMethods for selection</li>. "
                            + "<li>Response contains a JWT token that must be used to authenticate for further calls.</li>"
                            + "<li>This token can not be used to perform account access because the authentication process is not completed.</li>"
                            + "<li>Caller must proceed with the authCode endpoint: /{scaId}/authorisations/{authorisationId}/scaMethods/{scaMethodId}</li>"
                            + "</ul>"
                            + "</li>"
                            + "<li>PSUIDENTIFIED: the user exists but given password/pin did not match.</li>"
                            + "</ul>")
    @ApiResponses(value = {
            @ApiResponse(code = 200, response = SCALoginResponseTO.class, message = "Success. LoginToken contained in the returned response object."),
            @ApiResponse(code = 401, message = "Wrong authentication credential."),
            @ApiResponse(code = 403, message = "Authenticated but user does not have the requested role.")
    })
    ResponseEntity<SCALoginResponseTO> authorise(
            @RequestParam("login") String login,
            @RequestParam("pin") String pin,
            @RequestParam("role") UserRoleTO role);

    /**
     * Initiates the user login process. Returns a login response object describing how to proceed.
     * <p>
     * This response object contains an scaId that must be used to proceed with the login.
     * <p>
     * if the {@link SCALoginResponseTO#getScaStatus()} equals
     * {@link ScaStatusTO#EXEMPTED} the response will contain the final bearer token.
     * {@link ScaStatusTO#SCAMETHODSELECTED} means the auth code has been sent to the user. Must be entered by the user.
     * {@link ScaStatusTO#PSUAUTHENTICATED} there will be a list of scaMethods for selection in the response.
     * {@link ScaStatusTO#PSUIDENTIFIED} the user exists but given password/pin did not match.
     *
     * @param login           users login
     * @param pin             users pin
     * @param consentId       identifier of consent at CMS or other consent management system
     * @param authorisationId the identifier of authorization object
     * @param opType          the type of carried operation
     * @return ScaLoginResponse object containing information for further SCA validation if required and a valid access token
     */
    @PostMapping("/loginForConsent")
    @ApiOperation(tags = UnprotectedEndpoint.UNPROTECTED_ENDPOINT, value = "Login For Consent",
            notes = "Initiates the user login process for a payment or account information process. "
                            + "Returns a login response object describing how to proceed. "
                            + "This response object contains both an paymentId (consentId) and an authorizationId that must be used to identify this corresponding process.<br/>"
                            + "This response also contains an scaStatus that indicates the next stept to take."
                            + "<ul>"
                            + "<li>EXEMPTED: the operation to execute is exempted from sca. The operation can be complete in this single step. </li>"
                            + "<li>PSUAUTHENTICATED: the user has one or many sca methods configured for the operation."
                            + "<ul>"
                            + "<li>The response contains a list of scaMethods for selection</li>. "
                            + "<li>Response contains a JWT token that must be used to authenticate for further calls.</li>"
                            + "<li>This token can not be used to perform account access because the authentication process is not completed.</li>"
                            + "<li>Caller must proceed with the sca selection</li>"
                            + "</ul>"
                            + "</li>"
                            + "</ul>")
    @ApiResponses(value = {
            @ApiResponse(code = 200, response = SCALoginResponseTO.class, message = "Success. LoginToken contained in the returned response object."),
            @ApiResponse(code = 401, message = "Wrong authentication credential."),
            @ApiResponse(code = 403, message = "Authenticated but user does not have the requested role.")
    })
    ResponseEntity<SCALoginResponseTO> authoriseForConsent(
            @RequestParam("login") String login,
            @RequestParam("pin") String pin,
            @RequestParam("consentId") String consentId,
            @RequestParam("authorisationId") String authorisationId,
            @RequestParam("opType") OpTypeTO opType);

    @PostMapping("/loginForConsent/oauth")
    @ApiOperation(value = "Login for consent operation with bearer token", authorizations = @Authorization(value = "apiKey"))
    @ApiResponses(value = {
            @ApiResponse(code = 200, response = SCALoginResponseTO.class, message = "Success. LoginToken contained in the returned response object."),
            @ApiResponse(code = 401, message = "Wrong authentication credential."),
            @ApiResponse(code = 403, message = "Authenticated but user does not have the requested role.")
    })
    ResponseEntity<SCALoginResponseTO> authoriseForConsent(
            @RequestParam("consentId") String consentId,
            @RequestParam("authorisationId") String authorisationId,
            @RequestParam("opType") OpTypeTO opType);

    @PostMapping("/validate")
    @ApiOperation(tags = UnprotectedEndpoint.UNPROTECTED_ENDPOINT, value = "Introspect Token", nickname = "IntrospectToken",
            notes = "Validates a JWT access token and make sure permissions contained in this token are still in synch with the state of permission associated with the underlying user."
                            + "<ul>"
                            + "<li>This endpoint can optionaly be used by a presentation layer to revalidate a long living token like an AIS Consent token that can last up to 90 days.</li>"
                            + "<li>Response is an introspected access token object that can associated with the user request in the server."
                            + "</ul>")
    ResponseEntity<BearerTokenTO> validate(@RequestParam("accessToken") String token);

    //==========================================================================================================================
    //
    //	SELF SERVICE OPERATIONS. LOGIN TOKEN REQUIRED. SCA OPERATIONS
    //
    //==========================================================================================================================

    /**
     * Selects the scaMethod to use for sending a login transaction number to the user. This is only valid for the login.
     * <p>
     * The authorization id is used for the identification an authorization process.
     * <p>
     * Result must be {@link ScaStatusTO#SCAMETHODSELECTED} means the auth code has been sent to the user. Must be entered by the user.
     *
     * @param scaId           identifier of SCA operation at Ledgers
     * @param authorisationId identifier of Authorization at Ledgers
     * @param scaMethodId     identifier of SCA method
     * @return ScaLoginResponse object containing information for further SCA validation if required and an Access token
     */
    @PutMapping(value = "/{scaId}/authorisations/{authorisationId}/scaMethods/{scaMethodId}")
    @ApiOperation(value = "Select Sca Method",
            notes = "Selects the scaMethod to use for sending a an auth code to the user. "
                            + "<ul>"
                            + "<li>This endpoint is only valid for the login (can not be used for payment or account access).</li>"
                            + "<li>The call requires a JWT login token with matching scaId and authorisationId.</li>"
                            + "<li>The result of a sucessfull execution must be an SCALoginResponseTO object containing an scaStatus SCAMETHODSELECTED, indicating that an auth code has been generated and sent to the user.</li>"
                            + "<li>Caller must proceed with the authCode endpoint: /{scaId}/authorisations/{authorisationId}/authCode</li>"
                            + "</ul>",
            authorizations = @Authorization(value = "apiKey"))
    @ApiResponses(value = {
            @ApiResponse(code = 200, response = SCALoginResponseTO.class, message = "Authentication Code generated and sent through the selected method"),
            @ApiResponse(code = 422, message = "Wrong authorization code"),
            @ApiResponse(code = 406, message = "The given methodId is not supported for login process. This shall not happen as the preceeding call returns the list of method to select from."),
            @ApiResponse(code = 404, message = "Either the authorization instance is not found or the given scaMethodId does not exist."),
            @ApiResponse(code = 403, message = "Auth code consumed but user does not have the required role."),
            @ApiResponse(code = 401, message = "Provided bearer token could not be verified.")
    })
    ResponseEntity<SCALoginResponseTO> selectMethod(
            @ApiParam(name = "scaId", value = "The identifier of this login process. Contained in the SCALoginResponseTO received from the preceeding call.")
            @PathVariable(name = "scaId") String scaId,
            @ApiParam(name = "authorisationId", value = "The identifier of the authorisation instance of this login process. Generally identical to the scaId. But might differ if a login requires many authorizations. Contained in the SCALoginResponseTO received from the preceeding call.")
            @PathVariable("authorisationId") String authorisationId,
            @ApiParam(name = "scaMethodId", value = "methodId selected from the list of scaMethods contained in the SCALoginResponseTO received from the preceeding call.")
            @PathVariable("scaMethodId") String scaMethodId);

    /**
     * Send the auth code for two factor login. The returned response contains a bearer token that can be used
     * to authenticate further operations.
     *
     * @param scaId           identifier of SCA operation at Ledgers
     * @param authorisationId identifier of Authorization at Ledgers
     * @param authCode        a code sent to user to confirm operation
     * @return ScaLoginResponse with a valid Access Token to fulfill the requested operation
     */
    @PutMapping(value = "/{scaId}/authorisations/{authorisationId}/authCode")
    @ApiOperation(value = "Authorize Login",
            notes = "Sends the auth code for two factor login. The returned response contains a bearer "
                            + "token that can be used to authenticate further operations."
                            + "<ul>"
                            + "<li>This endpoint is only valid for the login (can not be used for payment or account access).</li>"
                            + "<li>The call requires a JWT login token with matching scaId and authorisationId.</li>"
                            + "<li>The result of a sucessfull execution must be an SCALoginResponseTO object containing an scaStatus SCAMETHODSELECTED, indicating that an auth code has been generated and sent to the user.</li>"
                            + "<li>Caller must proceed with the authCode endpoint: /{scaId}/authorisations/{authorisationId}/authCode</li>"
                            + "</ul>",
            authorizations = @Authorization(value = "apiKey"))
    @ApiResponses(value = {
            @ApiResponse(code = 200, response = SCALoginResponseTO.class, message = "Authentication complete. Returned access token can be used for further operations."),
            @ApiResponse(code = 410, message = "The provided authorization id is already consumed. Restart authentication."),
            @ApiResponse(code = 404, message = "Either the authorization instance is not found or the given scaMethodId does not exist."),
            @ApiResponse(code = 422, message = "The provided authorization code is wrong."),
            @ApiResponse(code = 406, message = "The given methodId is not supported for login process. This shall not happen as the preceeding call returns the list of method to select from."),
            @ApiResponse(code = 403, message = "Auth code consumed but user does not have the required role."),
            @ApiResponse(code = 401, message = "Provided bearer token could not be verified.")
    })
    ResponseEntity<SCALoginResponseTO> authorizeLogin(@PathVariable("scaId") String scaId,
                                                      @PathVariable("authorisationId") String authorisationId,
                                                      @RequestParam(name = "authCode") String authCode);

    //==========================================================================================================================
    //
    //	SELF SERVICE OPERATIONS. DIRECT ACCESS TOKEN REQUIRED.
    //
    //==========================================================================================================================
    @GetMapping("/me")
    @ApiOperation(value = "Current User", notes = "Retrieves the current usder."
                                                          + "<ul>"
                                                          + "<li>The idetifying information (userId=accessToken.sub) is implied from the security context information</li>"
                                                          + "<li>Will send back a 500 if the token is valid and the user is not found. This rather means that the user has been deleted since producing this token in a preceeding step might have implied the existence of the user.</li>"
                                                          + "</ul>",
            authorizations = @Authorization(value = "apiKey"))
    @ApiResponses(value = {
            @ApiResponse(code = 200, response = UserTO.class, message = "The user data record without the user pin."),
            @ApiResponse(code = 401, message = "Provided bearer token could not be verified.")
    })
    ResponseEntity<UserTO> getUser();

    @PutMapping("/sca-data")
    @ApiOperation(value = "Updates user SCA", notes = "Updates user authentication methods."
                                                              + "<lu>"
                                                              + "<li>User is implied from the provided access token.</li>"
                                                              + "<li>Actor token (delegation token like ais cosent token) can not be used to execute this operation</li>"
                                                              + "</ul>",
            authorizations = @Authorization(value = "apiKey"))
    @ApiResponses(value = {
            @ApiResponse(code = 200, response = Void.class, message = "The user data record without the user pin."),
            @ApiResponse(code = 401, message = "Provided bearer token could not be verified."),
            @ApiResponse(code = 403, message = "Provided bearer token not qualified for this operation."),
    })
    ResponseEntity<Void> updateUserScaData(@RequestBody List<ScaUserDataTO> data);

    //==========================================================================================================================
    //
    //	USER MANAGEMENT OPERATIONS. ACCESS TOKEN FROM STAFF, SYSTEM REQUIRED
    //
    //==========================================================================================================================
    @GetMapping("/{userId}")
    @ApiOperation(value = "Retrieves User by ID", notes = "Retrieves User by ID"
                                                                  + "<lu>"
                                                                  + "<li>This can only be called by either SYSTEM or STAFF members.</li>"
                                                                  + "<li>Will be moved to a management interface in the future.</li>"
                                                                  + "</lu>",
            authorizations = @Authorization(value = "apiKey"))
    ResponseEntity<UserTO> getUserById(@PathVariable("userId") String userId);

    @GetMapping
    @ApiOperation(value = "Lists users collection", notes = "Lists users collection."
                                                                    + "<lu>"
                                                                    + "<li>This can only be called by either SYSTEM or STAFF members.</li>"
                                                                    + "<li>Will be changed to include pagination and moved to a management interface in the future.</li>"
                                                                    + "</lu>",
            authorizations = @Authorization(value = "apiKey"))
    ResponseEntity<List<UserTO>> getAllUsers();
}
