package de.adorsys.ledgers.middleware.rest.resource;

import de.adorsys.ledgers.middleware.api.domain.general.RevertRequestTO;
import de.adorsys.ledgers.middleware.api.domain.um.AccountAccessTO;
import de.adorsys.ledgers.middleware.api.domain.um.ScaUserDataTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
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

import static de.adorsys.ledgers.middleware.rest.utils.Constants.*;

@Tag(name = "LDG010 - User Management (STAFF access)")
public interface UserMgmtStaffResourceAPI {
    String BASE_PATH = "/staff-access" + UserMgmtRestAPI.BASE_PATH;

    /**
     * Registers a new user within a given branch.
     *
     * @return user object without pin
     */
    @Operation(tags = UNPROTECTED_ENDPOINT, summary = "Register", description = "Registers a new user for a given branch.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = UserTO.class)), description = "The user data record without the pin."),
            @ApiResponse(responseCode = "409", description = "Conflict. A record with the given email or login already exists.")
    })
    @PostMapping("/register")
    ResponseEntity<UserTO> register(@RequestParam(BRANCH) String branch, @RequestBody UserTO branchStaff);

    /**
     * Modify a user within a given branch.
     *
     * @return user object without pin
     */
    @Operation(summary = "Modify user",
            description = "Modify existing user within the same branch as creator.")
    @SecurityRequirement(name = API_KEY)
    @SecurityRequirement(name = OAUTH2)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = UserTO.class)), description = "Success. Updated user is provided in the response."),
            @ApiResponse(responseCode = "401", description = "Wrong authentication credential."),
            @ApiResponse(responseCode = "403", description = "Authenticated but user does not have the requested role.")
    })
    @PostMapping("/modify")
    ResponseEntity<UserTO> modifyUser(@RequestParam(BRANCH) String branch, @RequestBody UserTO user);

    /**
     * Creates new user within the same branch
     *
     * @param user user to be created
     * @return created user
     */
    @Operation(summary = "Create user",
            description = "Create new user with the same branch as creator.")
    @SecurityRequirement(name = API_KEY)
    @SecurityRequirement(name = OAUTH2)
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
    @SecurityRequirement(name = API_KEY)
    @SecurityRequirement(name = OAUTH2)
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
    @SecurityRequirement(name = API_KEY)
    @SecurityRequirement(name = OAUTH2)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = UserTO.class)), description = "Success. List of logins received."),
            @ApiResponse(responseCode = "401", description = "Wrong authentication credential."),
            @ApiResponse(responseCode = "403", description = "Authenticated but user does not have the requested role.")
    })
    @GetMapping("/logins")
    ResponseEntity<List<String>> getBranchUserLogins();

    /**
     * Get list of user logins for the given branch.
     *
     * @return list of user logins.
     */
    @Operation(summary = "List user logins by branchId",
            description = "List user logins by branchId.")
    @SecurityRequirement(name = API_KEY)
    @SecurityRequirement(name = OAUTH2)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = UserTO.class)), description = "Success. List of logins received."),
            @ApiResponse(responseCode = "401", description = "Wrong authentication credential."),
            @ApiResponse(responseCode = "403", description = "Authenticated but user does not have the requested role.")
    })
    @GetMapping("/logins/{branchId}")
    ResponseEntity<List<String>> getBranchUserLoginsByBranchId(@PathVariable(BRANCH_ID) String branchId);

    /**
     * Gets user by ID if it's within the branch
     *
     * @param userId user ID
     * @return user
     */
    @Operation(summary = "Gets user by ID if it's within the branch",
            description = "Gets user by ID if it's within the branch.")
    @SecurityRequirement(name = API_KEY)
    @SecurityRequirement(name = OAUTH2)
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
    @SecurityRequirement(name = API_KEY)
    @SecurityRequirement(name = OAUTH2)
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
    @SecurityRequirement(name = API_KEY)
    @SecurityRequirement(name = OAUTH2)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success. Account Access Successfully updated."),
            @ApiResponse(responseCode = "401", description = "Wrong authentication credential."),
            @ApiResponse(responseCode = "403", description = "Authenticated but user does not have the requested role.")
    })
    @PutMapping("/access/{userId}")
    ResponseEntity<Void> updateAccountAccessForUser(@PathVariable(USER_ID) String userId, @RequestBody AccountAccessTO access);

    @Operation(summary = "Block/Unblock user",
            description = "Changes block state for given user, returns status being set to the block")
    @SecurityRequirement(name = API_KEY)
    @SecurityRequirement(name = OAUTH2)
    @PostMapping("/{userId}/status")
    ResponseEntity<Boolean> changeStatus(@PathVariable(USER_ID) String userId);


    @PostMapping("/revert")
    @Operation(summary = "Reverts DB state for given user to the given date and time.")
    @SecurityRequirement(name = API_KEY)
    @SecurityRequirement(name = OAUTH2)
    ResponseEntity<Void> revertDatabase(@RequestBody RevertRequestTO revertRequest);

}
