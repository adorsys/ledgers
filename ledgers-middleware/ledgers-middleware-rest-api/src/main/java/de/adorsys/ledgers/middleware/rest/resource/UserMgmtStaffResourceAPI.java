package de.adorsys.ledgers.middleware.rest.resource;

import de.adorsys.ledgers.middleware.api.domain.general.RevertRequestTO;
import de.adorsys.ledgers.middleware.api.domain.oauth.AuthoriseForUserTO;
import de.adorsys.ledgers.middleware.api.domain.sca.SCALoginResponseTO;
import de.adorsys.ledgers.middleware.api.domain.um.*;
import de.adorsys.ledgers.util.domain.CustomPageImpl;
import io.swagger.annotations.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//@Api(tags = "LDG010 - User Management (STAFF access)")
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
    @Operation(tags = UnprotectedEndpoint.UNPROTECTED_ENDPOINT, value = "Register", notes = "Registers a new user for a given branch.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, response = UserTO.class, message = "The user data record without the pin."),
            @ApiResponse(code = 409, message = "Conflict. A record with the given email or login already exists.")
    })
    @PostMapping("/register")
    ResponseEntity<UserTO> register(@RequestParam(BRANCH) String branch, @RequestBody UserTO branchStaff);

    @Operation(tags = UnprotectedEndpoint.UNPROTECTED_ENDPOINT, value = "Login fo user")
    @ApiResponses(value = {
            @ApiResponse(code = 200, response = UserTO.class, message = "The user data record without the pin."),
            @ApiResponse(code = 404, message = "User not found.")
    })
    @PostMapping("/admin/authorize/user")
    ResponseEntity<SCALoginResponseTO> authoriseForUser(@RequestBody AuthoriseForUserTO authorise);

    /**
     * Modify a user within a given branch.
     *
     * @return user object without pin
     */
    @Operation(value = "Modify user",
            notes = "Modify existing user within the same branch as creator.",
            authorizations = @Authorization(value = "apiKey"))
    @ApiResponses(value = {
            @ApiResponse(code = 200, response = UserTO.class, message = "Success. Updated user is provided in the response."),
            @ApiResponse(code = 401, message = "Wrong authentication credential."),
            @ApiResponse(code = 403, message = "Authenticated but user does not have the requested role.")
    })
    @PostMapping("/modify")
    ResponseEntity<UserTO> modifyUser(@RequestParam(BRANCH) String branch, @RequestBody UserTO user);

    /**
     * Authorize returns a bearer token that can be reused by the consuming application.
     *
     * @param userCredentials tpp login and tpp pin
     * @return JWT token and user info
     */
    @Operation(tags = UnprotectedEndpoint.UNPROTECTED_ENDPOINT, value = "Login",
            notes = "Initiates the user login process. Returns a login response object describing how to proceed.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, response = SCALoginResponseTO.class, message = "Success. LoginToken contained in the returned response object."),
            @ApiResponse(code = 401, message = "Wrong authentication credential."),
            @ApiResponse(code = 403, message = "Authenticated but user does not have the requested role.")
    })
    @PostMapping("/login")
    ResponseEntity<SCALoginResponseTO> login(@RequestBody UserCredentialsTO userCredentials);

    /**
     * Creates new user within the same branch
     *
     * @param user user to be created
     * @return created user
     */
    @Operation(value = "Create user",
            notes = "Create new user with the same branch as creator.",
            authorizations = @Authorization(value = "apiKey"))
    @ApiResponses(value = {
            @ApiResponse(code = 200, response = UserTO.class, message = "Success. Created user in provided in the response."),
            @ApiResponse(code = 401, message = "Wrong authentication credential."),
            @ApiResponse(code = 403, message = "Authenticated but user does not have the requested role.")
    })

    @PostMapping
    ResponseEntity<UserTO> createUser(@RequestBody UserTO user);

    /**
     * Lists users within the branch and roles
     *
     * @return list of users for the branch with roles
     */
    @Operation(value = "Lists users by branch and role",
            notes = "Lists users by branch and roles.",
            authorizations = @Authorization(value = "apiKey"))
    @ApiResponses(value = {
            @ApiResponse(code = 200, response = UserTO.class, message = "Success. Created user in provided in the response."),
            @ApiResponse(code = 401, message = "Wrong authentication credential."),
            @ApiResponse(code = 403, message = "Authenticated but user does not have the requested role.")
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
    @Operation(value = "Lists user logins by branch",
            notes = "Lists user logins by branch.",
            authorizations = @Authorization(value = "apiKey"))
    @ApiResponses(value = {
            @ApiResponse(code = 200, response = UserTO.class, message = "Success. List of logins received."),
            @ApiResponse(code = 401, message = "Wrong authentication credential."),
            @ApiResponse(code = 403, message = "Authenticated but user does not have the requested role.")
    })
    @GetMapping("/logins")
    ResponseEntity<List<String>> getBranchUserLogins();

    /**
     * Gets user by ID if it's within the branch
     *
     * @param userId user ID
     * @return user
     */
    @Operation(value = "Gets user by ID if it's within the branch",
            notes = "Gets user by ID if it's within the branch.",
            authorizations = @Authorization(value = "apiKey"))
    @ApiResponses(value = {
            @ApiResponse(code = 200, response = UserTO.class, message = "Success. Created user in provided in the response."),
            @ApiResponse(code = 401, message = "Wrong authentication credential."),
            @ApiResponse(code = 403, message = "Authenticated but user does not have the requested role.")
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
    @Operation(value = "Updates SCA Data for user if it's within the branch.",
            notes = "Updates SCA Data for user if it's within the branch.",
            authorizations = @Authorization(value = "apiKey"))
    @ApiResponses(value = {
            @ApiResponse(code = 200, response = UserTO.class, message = "Success. Created user in provided in the response."),
            @ApiResponse(code = 401, message = "Wrong authentication credential."),
            @ApiResponse(code = 403, message = "Authenticated but user does not have the requested role.")
    })
    @PostMapping("/{userId}/sca-data")
    ResponseEntity<Void> updateUserScaData(@PathVariable(USER_ID) String userId, @RequestBody List<ScaUserDataTO> data);

    /**
     * Grants/Updates AccountAccess for a User for a Certain account within the branch
     *
     * @return nothing
     */
    @Operation(value = "Grants/Updates Account Access for user.",
            notes = "Grants/Updates Account Access for user.",
            authorizations = @Authorization(value = "apiKey"))
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success. Account Access Successfully updated."),
            @ApiResponse(code = 401, message = "Wrong authentication credential."),
            @ApiResponse(code = 403, message = "Authenticated but user does not have the requested role.")
    })
    @PutMapping("/access/{userId}")
    ResponseEntity<Void> updateAccountAccessForUser(@PathVariable(USER_ID) String userId, @RequestBody AccountAccessTO access);

    @Operation(value = "Block/Unblock user",
            notes = "Changes block state for given user, returns status being set to the block",
            authorizations = @Authorization(value = "apiKey"))
    @PostMapping("/{userId}/status")
    ResponseEntity<Boolean> changeStatus(@PathVariable(USER_ID) String userId);


    @PostMapping("/revert")
    @Operation(value = "Reverts DB state for given user to the given date and time.", authorizations = @Authorization(value = "apiKey"))
    ResponseEntity<Void> revertDatabase(@RequestBody RevertRequestTO revertRequest);

}
