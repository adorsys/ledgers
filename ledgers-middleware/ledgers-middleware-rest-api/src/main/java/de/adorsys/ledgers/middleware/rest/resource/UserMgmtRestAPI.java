/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.rest.resource;

import de.adorsys.ledgers.middleware.api.domain.account.AccountReferenceTO;
import de.adorsys.ledgers.middleware.api.domain.sca.AuthConfirmationTO;
import de.adorsys.ledgers.middleware.api.domain.um.ScaUserDataTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static de.adorsys.ledgers.middleware.rest.utils.Constants.*;

@Tag(name = "LDG002 - User Management", description = "Provides endpoint for registering, authorizing and managing users.")
public interface UserMgmtRestAPI {
    String BASE_PATH = "/users";

    //==========================================================================================================================
    //
    //	SELF SERVICE OPERATIONS. NO CREDENTIAL REQUIRED
    //
    //==========================================================================================================================

    @GetMapping("/multilevel")
    @Operation(tags = UNPROTECTED_ENDPOINT, summary = "Check if multilevel SCA required for certain user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Boolean.class)), description = "Boolean representation of requirement for multi-level sca")
    })
    ResponseEntity<Boolean> multilevel(@RequestParam(LOGIN) String login,
                                       @RequestParam(IBAN) String iban);

    @PostMapping("/multilevel")
    @Operation(tags = UNPROTECTED_ENDPOINT, summary = "Check if multilevel SCA required for certain user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = Boolean.class)), description = "Boolean representation of requirement for multi-level sca")
    })
    ResponseEntity<Boolean> multilevelAccounts(@RequestParam(LOGIN) String login, @RequestBody List<AccountReferenceTO> references);

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
    @Operation(tags = UNPROTECTED_ENDPOINT, summary = "Register", description = "Registers a user."
                                                                                        + "<ul>"
                                                                                        + "<li>A user is always registered as customer and is activated by default.</li>"
                                                                                        + "<li>A user can only be given another role by an administrating STAFF member.</li>"
                                                                                        + "</ul>")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = UserTO.class)), description = "The user data record without the user pin."),
            @ApiResponse(responseCode = "409", description = "Conflict. A user with email or login name already exist.")
    })
    ResponseEntity<UserTO> register(@RequestParam(LOGIN) String login,
                                    @RequestParam(EMAIL) String email,
                                    @RequestParam(PIN) String pin,
                                    // TODO remove role parameter.
                                    @RequestParam(name = ROLE, defaultValue = "CUSTOMER") UserRoleTO role);

    //==========================================================================================================================
    //
    //	SELF SERVICE OPERATIONS. DIRECT ACCESS TOKEN REQUIRED.
    //
    //==========================================================================================================================
    @GetMapping("/me")
    @Operation(summary = "Current User", description = "Retrieves the current usder."
                                                               + "<ul>"
                                                               + "<li>The idetifying information (userId=accessToken.sub) is implied from the security context information</li>"
                                                               + "<li>Will send back a 500 if the token is valid and the user is not found. This rather means that the user has been deleted since producing this token in a preceeding step might have implied the existence of the user.</li>"
                                                               + "</ul>")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = UserTO.class)), description = "The user data record without the user pin."),
            @ApiResponse(responseCode = "401", description = "Provided bearer token could not be verified.")
    })
    @SecurityRequirement(name = API_KEY)
    @SecurityRequirement(name = OAUTH2)
    ResponseEntity<UserTO> getUser();

    @PutMapping("/me")
    @Operation(summary = "Edit current User")
    @SecurityRequirement(name = API_KEY)
    @SecurityRequirement(name = OAUTH2)
    ResponseEntity<Void> editSelf(@RequestBody UserTO user);

    @PutMapping("/sca-data")
    @Operation(summary = "Updates user SCA", description = "Updates user authentication methods."
                                                                   + "<lu>"
                                                                   + "<li>User is implied from the provided access token.</li>"
                                                                   + "<li>Actor token (delegation token like ais cosent token) can not be used to execute this operation</li>"
                                                                   + "</ul>")
    @SecurityRequirement(name = API_KEY)
    @SecurityRequirement(name = OAUTH2)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The user data record without the user pin."),
            @ApiResponse(responseCode = "401", description = "Provided bearer token could not be verified."),
            @ApiResponse(responseCode = "403", description = "Provided bearer token not qualified for this operation."),
    })
    ResponseEntity<Void> updateUserScaData(@RequestBody List<ScaUserDataTO> data);

    //==========================================================================================================================
    //
    //	USER MANAGEMENT OPERATIONS. ACCESS TOKEN FROM STAFF, SYSTEM REQUIRED
    //
    //==========================================================================================================================
    @GetMapping("/{userId}")
    @Operation(summary = "Retrieves User by ID", description = "Retrieves User by ID"
                                                                       + "<lu>"
                                                                       + "<li>This can only be called by either SYSTEM or STAFF members.</li>"
                                                                       + "<li>Will be moved to a management interface in the future.</li>"
                                                                       + "</lu>")
    @SecurityRequirement(name = API_KEY)
    @SecurityRequirement(name = OAUTH2)
    ResponseEntity<UserTO> getUserById(@PathVariable(USER_ID) String userId);

    @PutMapping("/authorisations/{authorisationId}/confirmation/{authConfirmCode}")
    @Operation(summary = "Send an authentication confirmation code for validation", description = "Validate an authentication code")
    @SecurityRequirement(name = API_KEY)
    @SecurityRequirement(name = OAUTH2)
    ResponseEntity<AuthConfirmationTO> verifyAuthConfirmationCode(@PathVariable(AUTH_ID) String authorisationId,
                                                                  @PathVariable(name = AUTH_CONF_CODE) String authConfirmCode);

    @PutMapping("/authorisations/{authorisationId}/confirmation")
    @Operation(summary = "Send an authentication confirmation code for validation", description = "Validate an authentication code")
    @SecurityRequirement(name = API_KEY)
    @SecurityRequirement(name = OAUTH2)
    ResponseEntity<AuthConfirmationTO> completeAuthConfirmation(@PathVariable(AUTH_ID) String authorisationId,
                                                                @RequestParam(value = AUTH_CONFIRMED, defaultValue = "false") boolean authCodeConfirmed);

    @PostMapping("/reset/password/{login}")
    @Operation(summary = "Reset password via email", description = "Send link for password reset to user email.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Send link to user email for password reset."),
            @ApiResponse(responseCode = "404", description = "Conflict. A user with email not found.")
    })
    ResponseEntity<Void> resetPasswordViaEmail(@PathVariable(LOGIN) String login);
}
