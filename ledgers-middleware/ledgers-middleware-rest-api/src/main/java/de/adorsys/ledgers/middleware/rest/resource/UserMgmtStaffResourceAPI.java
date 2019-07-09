package de.adorsys.ledgers.middleware.rest.resource;

import de.adorsys.ledgers.middleware.api.domain.sca.SCALoginResponseTO;
import de.adorsys.ledgers.middleware.api.domain.um.*;
import de.adorsys.ledgers.middleware.rest.exception.ConflictRestException;
import io.swagger.annotations.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "LDG007 - User Management (STAFF access)",
        description = "Provides endpoint for registering, authorizing and managing users by staff management")
public interface UserMgmtStaffResourceAPI {
    String BASE_PATH = "/staff-access" + UserMgmtRestAPI.BASE_PATH;
    String BRANCH = "branch";
    String ROLES = "roles";
    String USER_ID = "userId";
    String USER_NOT_IN_BRANCH = "User is not your branch";
    String USER_CANNOT_REGISTER_IN_BRANCH = "User cannot register for this branch. The branch is occupied by other user";
    String USER_EMAIL_OR_LOGIN_TAKEN = "Provided email or login are already taken";

    /**
     * Registers a new user within a given branch.
     *
     * @return user object without pin
     * @throws ConflictRestException user with same login or email already exists
     */
    @ApiOperation(tags = UnprotectedEndpoint.UNPROTECTED_ENDPOINT, value = "Register", notes = "Registers a new user for a given branch.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, response = UserTO.class, message = "The user data record without the pin."),
            @ApiResponse(code = 409, message = "Conflict. A record with the given email or login already exists.")
    })
    @PostMapping("/register")
    ResponseEntity<UserTO> register(@RequestParam(BRANCH) String branch, @RequestBody UserTO branchStaff);

    /**
     * Authorize returns a bearer token that can be reused by the consuming application.
     *
     * @param userCredentials tpp login and tpp pin
     * @return JWT token and user info
     */
    @ApiOperation(tags = UnprotectedEndpoint.UNPROTECTED_ENDPOINT, value = "Login",
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
    @ApiOperation(value = "Create user",
            notes = "Create new user with the same branch as creator.",
            authorizations = @Authorization(value = "apiKey"))
    @ApiResponses(value = {
            @ApiResponse(code = 200, response = UserTO.class, message = "Success. Created user in provided in the response."),
            @ApiResponse(code = 401, message = "Wrong authentication credential."),
            @ApiResponse(code = 403, message = "Authenticated but user does not have the requested role.")
    })

    @PostMapping
    ResponseEntity<UserTO> createUser(@RequestBody UserTO user);

    // TODO: pagination for users and limit users for branch

    /**
     * Lists users within the branch and roles
     *
     * @return list of users for the branch with roles
     */
    @ApiOperation(value = "Lists users by branch and role",
            notes = "Lists users by branch and roles.",
            authorizations = @Authorization(value = "apiKey"))
    @ApiResponses(value = {
            @ApiResponse(code = 200, response = UserTO.class, message = "Success. Created user in provided in the response."),
            @ApiResponse(code = 401, message = "Wrong authentication credential."),
            @ApiResponse(code = 403, message = "Authenticated but user does not have the requested role.")
    })

    @GetMapping
    ResponseEntity<List<UserTO>> getBranchUsersByRoles(@RequestParam(ROLES) List<UserRoleTO> roles);

    /**
     * Gets user by ID if it's within the branch
     *
     * @param userId user ID
     * @return user
     */
    @ApiOperation(value = "Gets user by ID if it's within the branch",
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
    @ApiOperation(value = "Updates SCA Data for user if it's within the branch.",
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
    @ApiOperation(value = "Grants/Updates Account Access for user.",
            notes = "Grants/Updates Account Access for user.",
            authorizations = @Authorization(value = "apiKey"))
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success. Account Access Successfully updated."),
            @ApiResponse(code = 401, message = "Wrong authentication credential."),
            @ApiResponse(code = 403, message = "Authenticated but user does not have the requested role.")
    })
    @PutMapping("/access/{userId}")
    ResponseEntity<Void> updateAccountAccessForUser(@PathVariable(USER_ID) String userId, @RequestBody AccountAccessTO access);
}
