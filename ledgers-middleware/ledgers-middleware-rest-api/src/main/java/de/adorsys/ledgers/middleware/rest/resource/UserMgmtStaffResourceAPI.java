package de.adorsys.ledgers.middleware.rest.resource;

import de.adorsys.ledgers.middleware.api.domain.general.RevertRequestTO;
import de.adorsys.ledgers.middleware.api.domain.oauth.AuthoriseForUserTO;
import de.adorsys.ledgers.middleware.api.domain.sca.SCALoginResponseTO;
import de.adorsys.ledgers.middleware.api.domain.um.*;
import de.adorsys.ledgers.util.domain.CustomPageImpl;
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

@Tag(name = "LDG010 - User Management (STAFF access)")
public interface UserMgmtStaffResourceAPI {
    String BASE_PATH = "/staff-access" + UserMgmtRestAPI.BASE_PATH;
    String BRANCH = "branch";
    String ROLES = "roles";
    String QUERY_PARAM = "queryParam";
    String BLOCKED = "blockedParam";
    String PAGE = "page";
    String SIZE = "size";
    String USER_ID = "userId";
    String USER_NOT_IN_BRANCH = "User is not your branch";
    String USER_CANNOT_REGISTER_IN_BRANCH = "User cannot register for this branch. The branch is occupied by other user";

    /**
     * Registers a new user within a given branch.
     *
     * @return user object without pin
     */
    @Operation(tags = UnprotectedEndpoint.UNPROTECTED_ENDPOINT, summary = "Register", description = "Registers a new user for a given branch.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = UserTO.class)), description = "The user data record without the pin."),
            @ApiResponse(responseCode = "409", description = "Conflict. A record with the given email or login already exists.")
    })
    @PostMapping("/register")
    ResponseEntity<UserTO> register(@RequestParam(BRANCH) String branch, @RequestBody UserTO branchStaff);

    @Operation(tags = UnprotectedEndpoint.UNPROTECTED_ENDPOINT, summary = "Login fo user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = UserTO.class)), description = "The user data record without the pin."),
            @ApiResponse(responseCode = "404", description = "User not found.")
    })
    @PostMapping("/admin/authorize/user")
    ResponseEntity<SCALoginResponseTO> authoriseForUser(@RequestBody AuthoriseForUserTO authorise);

    /**
     * Modify a user within a given branch.
     *
     * @return user object without pin
     */
    @Operation(summary = "Modify user",
            description = "Modify existing user within the same branch as creator.")
    @SecurityRequirement(name = "Authorization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = UserTO.class)), description = "Success. Updated user is provided in the response."),
            @ApiResponse(responseCode = "401", description = "Wrong authentication credential."),
            @ApiResponse(responseCode = "403", description = "Authenticated but user does not have the requested role.")
    })
    @PostMapping("/modify")
    ResponseEntity<UserTO> modifyUser(@RequestParam(BRANCH) String branch, @RequestBody UserTO user);

    /**
     * Authorize returns a bearer token that can be reused by the consuming application.
     *
     * @param userCredentials tpp login and tpp pin
     * @return JWT token and user info
     */
    @Operation(tags = UnprotectedEndpoint.UNPROTECTED_ENDPOINT, summary = "Login",
            description = "Initiates the user login process. Returns a login response object describing how to proceed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = SCALoginResponseTO.class)), description = "Success. LoginToken contained in the returned response object."),
            @ApiResponse(responseCode = "401", description = "Wrong authentication credential."),
            @ApiResponse(responseCode = "403", description = "Authenticated but user does not have the requested role.")
    })
    @PostMapping("/login")
    ResponseEntity<SCALoginResponseTO> login(@RequestBody UserCredentialsTO userCredentials);

    /**
     * Creates new user within the same branch
     *
     * @param user user to be created
     * @return created user
     */
    @Operation(summary = "Create user",
            description = "Create new user with the same branch as creator.")
    @SecurityRequirement(name = "Authorization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = UserTO.class)), description = "Success. Created user in provided in the response."),
            @ApiResponse(responseCode = "401", description = "Wrong authentication credential."),
            @ApiResponse(responseCode = "403", description = "Authenticated but user does not have the requested role.")
    })

    @PostMapping
    ResponseEntity<UserTO> createUser(@RequestBody UserTO user);

    /**
     * Lists users within the branch and roles
     *
     * @return list of users for the branch with roles
     */
    @Operation(summary = "Lists users by branch and role",
            description = "Lists users by branch and roles.")
    @SecurityRequirement(name = "Authorization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = UserTO.class)), description = "Success. Created user in provided in the response."),
            @ApiResponse(responseCode = "401", description = "Wrong authentication credential."),
            @ApiResponse(responseCode = "403", description = "Authenticated but user does not have the requested role.")
    })

    @GetMapping
    ResponseEntity<CustomPageImpl<UserTO>> getBranchUsersByRoles(
            @RequestParam(ROLES) List<UserRoleTO> roles,
            @RequestParam(value = QUERY_PARAM, defaultValue = "", required = false) String queryParam,
            @RequestParam(value = BLOCKED, required = false) Boolean blockedParam,
            @RequestParam(PAGE) int page, @RequestParam(SIZE) int size);

    /**
     * Get list of user logins within the branch.
     *
     * @return list of user logins.
     */
    @Operation(summary = "Lists user logins by branch",
            description = "Lists user logins by branch.")
    @SecurityRequirement(name = "Authorization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = UserTO.class)), description = "Success. List of logins received."),
            @ApiResponse(responseCode = "401", description = "Wrong authentication credential."),
            @ApiResponse(responseCode = "403", description = "Authenticated but user does not have the requested role.")
    })
    @GetMapping("/logins")
    ResponseEntity<List<String>> getBranchUserLogins();

    /**
     * Gets user by ID if it's within the branch
     *
     * @param userId user ID
     * @return user
     */
    @Operation(summary = "Gets user by ID if it's within the branch",
            description = "Gets user by ID if it's within the branch.")
    @SecurityRequirement(name = "Authorization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = UserTO.class)), description = "Success. Created user in provided in the response."),
            @ApiResponse(responseCode = "401", description = "Wrong authentication credential."),
            @ApiResponse(responseCode = "403", description = "Authenticated but user does not have the requested role.")
    })
    @GetMapping("/{userId}")
    ResponseEntity<UserTO> getBranchUserById(@PathVariable(USER_ID) String userId);

    /**
     * Updates SCA Data for user if it's within the branch
     *
     * @param userId user ID
     * @param data   user SCA data
     * @return updated user
     */
    @Operation(summary = "Updates SCA Data for user if it's within the branch.",
            description = "Updates SCA Data for user if it's within the branch.")
    @SecurityRequirement(name = "Authorization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = UserTO.class)), description = "Success. Created user in provided in the response."),
            @ApiResponse(responseCode = "401", description = "Wrong authentication credential."),
            @ApiResponse(responseCode = "403", description = "Authenticated but user does not have the requested role.")
    })
    @PostMapping("/{userId}/sca-data")
    ResponseEntity<Void> updateUserScaData(@PathVariable(USER_ID) String userId, @RequestBody List<ScaUserDataTO> data);

    /**
     * Grants/Updates AccountAccess for a User for a Certain account within the branch
     *
     * @return nothing
     */
    @Operation(summary = "Grants/Updates Account Access for user.",
            description = "Grants/Updates Account Access for user.")
    @SecurityRequirement(name = "Authorization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success. Account Access Successfully updated."),
            @ApiResponse(responseCode = "401", description = "Wrong authentication credential."),
            @ApiResponse(responseCode = "403", description = "Authenticated but user does not have the requested role.")
    })
    @PutMapping("/access/{userId}")
    ResponseEntity<Void> updateAccountAccessForUser(@PathVariable(USER_ID) String userId, @RequestBody AccountAccessTO access);

    @Operation(summary = "Block/Unblock user",
            description = "Changes block state for given user, returns status being set to the block")
    @SecurityRequirement(name = "Authorization")
    @PostMapping("/{userId}/status")
    ResponseEntity<Boolean> changeStatus(@PathVariable(USER_ID) String userId);


    @PostMapping("/revert")
    @Operation(summary = "Reverts DB state for given user to the given date and time.")
    @SecurityRequirement(name = "Authorization")
    ResponseEntity<Void> revertDatabase(@RequestBody RevertRequestTO revertRequest);

}
