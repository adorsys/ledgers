package de.adorsys.ledgers.middleware.rest.resource;


import de.adorsys.ledgers.middleware.api.domain.sca.SCALoginResponseTO;
import de.adorsys.ledgers.middleware.api.domain.um.ScaUserDataTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserCredentialsTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.exception.UserNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.rest.exception.ConflictRestException;
import de.adorsys.ledgers.middleware.rest.exception.ForbiddenRestException;
import de.adorsys.ledgers.middleware.rest.exception.NotFoundRestException;
import io.swagger.annotations.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "LDG007 - Branch Rest endpoint",
        description= "Provides endpoint for registering, authorizing and managing users within their branches")

public interface BranchRestApi {

    String BASE_PATH = "/branches";

    /**
     * Registers a new user within a given branch.
     *
     * @return user object without pin
     * @throws ConflictRestException user with same login or email already exists
     */
    @PostMapping("/register")
    @ApiOperation(tags=UnprotectedEndpoint.UNPROTECTED_ENDPOINT, value="Register", notes="Registers a new user for a given branch.")
    @ApiResponses(value={
            @ApiResponse(code=200, response= UserTO.class, message="The user data record without the pin."),
            @ApiResponse(code=409, message="Conflict. A record with the given email or login already exists.")
    })
    ResponseEntity<UserTO> register(@RequestParam String branch, @RequestBody UserTO user) throws ConflictRestException;


    /**
     * Authorize returns a bearer token that can be reused by the consuming application.
     *
     * @param userCredentials tpp login and tpp pin
     * @return JWT token and user info
     */
    @PostMapping("/login")
    @ApiOperation(tags=UnprotectedEndpoint.UNPROTECTED_ENDPOINT, value="Login",
            notes="Initiates the user login process. Returns a login response object describing how to proceed.")
    @ApiResponses(value={
            @ApiResponse(code=200, response=SCALoginResponseTO.class, message="Success. LoginToken contained in the returned response object."),
            @ApiResponse(code=401, message="Wrong authentication credential."),
            @ApiResponse(code=403, message="Authenticated but user does not have the requested role.")
    })
    ResponseEntity<SCALoginResponseTO> login(@RequestBody UserCredentialsTO userCredentials) throws NotFoundRestException, ForbiddenRestException;

    /**
     * Creates new user within the same branch
     *
     * @param user user to be created
     * @return created user
     */
    @PostMapping("/users")
    @ApiOperation(value="Create user",
            notes="Create new user with the same branch as creator.",
            authorizations =@Authorization(value="apiKey"))
    @ApiResponses(value={
            @ApiResponse(code=200, response=UserTO.class, message="Success. Created user in provided in the response."),
            @ApiResponse(code=401, message="Wrong authentication credential."),
            @ApiResponse(code=403, message="Authenticated but user does not have the requested role.")
    })
    ResponseEntity<UserTO> createUser(@RequestBody UserTO user) throws UserNotFoundMiddlewareException, ConflictRestException;

    /**
     * Lists users within the branch and roles
     *
     * @return list of users for the branch with roles
     */
    @GetMapping("/users")
    @ApiOperation(value="Lists users by branch and role",
            notes="Lists users by branch and roles.",
            authorizations =@Authorization(value="apiKey"))
    @ApiResponses(value={
            @ApiResponse(code=200, response=UserTO.class, message="Success. Created user in provided in the response."),
            @ApiResponse(code=401, message="Wrong authentication credential."),
            @ApiResponse(code=403, message="Authenticated but user does not have the requested role.")
    })
    ResponseEntity<List<UserTO>> getBranchUsersByRoles(@RequestParam List<UserRoleTO> roles) throws UserNotFoundMiddlewareException;


    /**
     * Gets user by ID if it's within the branch
     * @param userId user ID
     * @return user
     */
    @GetMapping("/users/{userId}")
    @ApiOperation(value="Gets user by ID if it's within the branch",
            notes="Gets user by ID if it's within the branch.",
            authorizations =@Authorization(value="apiKey"))
    @ApiResponses(value={
            @ApiResponse(code=200, response=UserTO.class, message="Success. Created user in provided in the response."),
            @ApiResponse(code=401, message="Wrong authentication credential."),
            @ApiResponse(code=403, message="Authenticated but user does not have the requested role.")
    })
    ResponseEntity<UserTO> getBranchUserById(@PathVariable String userId) throws UserNotFoundMiddlewareException;

    /**
     * Updates SCA Data for user if it's within the branch
     * @param userId user ID
     * @param data user SCA data
     * @return updated user
     */
    @PostMapping("/users/{userId}/sca-data")
    @ApiOperation(value="Updates SCA Data for user if it's within the branch.",
            notes="Updates SCA Data for user if it's within the branch.",
            authorizations =@Authorization(value="apiKey"))
    @ApiResponses(value={
            @ApiResponse(code=200, response=UserTO.class, message="Success. Created user in provided in the response."),
            @ApiResponse(code=401, message="Wrong authentication credential."),
            @ApiResponse(code=403, message="Authenticated but user does not have the requested role.")
    })
    ResponseEntity<Void> updateUserScaData(@PathVariable String userId, @RequestBody List<ScaUserDataTO> data);
}
