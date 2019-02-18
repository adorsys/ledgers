package de.adorsys.ledgers.middleware.rest.resource;

import de.adorsys.ledgers.middleware.api.domain.sca.SCALoginResponseTO;
import de.adorsys.ledgers.middleware.api.domain.um.*;
import de.adorsys.ledgers.middleware.api.exception.InsufficientPermissionMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.UserAlreadyExistsMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.UserNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.service.MiddlewareOnlineBankingService;
import de.adorsys.ledgers.middleware.api.service.MiddlewareUserManagementService;
import de.adorsys.ledgers.middleware.rest.annotation.MiddlewareUserResource;
import de.adorsys.ledgers.middleware.rest.exception.ConflictRestException;
import de.adorsys.ledgers.middleware.rest.exception.ForbiddenRestException;
import de.adorsys.ledgers.middleware.rest.exception.NotFoundRestException;
import io.swagger.annotations.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.List;

@Api(tags = "LDG007 - User Management (STAFF access)",
        description= "Provides endpoint for registering, authorizing and managing users by staff management")

@RestController
@RequestMapping("/staff-access" + UserMgmtRestAPI.BASE_PATH)
@MiddlewareUserResource
public class UserMgmtStaffResource {

    private final MiddlewareOnlineBankingService onlineBankingService;
    private final MiddlewareUserManagementService middlewareUserService;
    private final AccessTokenTO accessToken;
    private static final String USER_NOT_IN_BRANCH = "User is not your branch";
    private static final String USER_CANNOT_REGISTER_IN_BRANCH = "User cannot register for this branch. The branch is occupied by other user";

    public UserMgmtStaffResource(
            MiddlewareOnlineBankingService onlineBankingService,
            MiddlewareUserManagementService middlewareUserService,
            AccessTokenTO accessToken) {
        super();
        this.onlineBankingService = onlineBankingService;
        this.middlewareUserService = middlewareUserService;
        this.accessToken = accessToken;
    }

    /**
     * Registers a new user within a given branch.
     *
     * @return user object without pin
     * @throws ConflictRestException user with same login or email already exists
     */
    @ApiOperation(tags=UnprotectedEndpoint.UNPROTECTED_ENDPOINT, value="Register", notes="Registers a new user for a given branch.")
    @ApiResponses(value={
            @ApiResponse(code=200, response= UserTO.class, message="The user data record without the pin."),
            @ApiResponse(code=409, message="Conflict. A record with the given email or login already exists.")
    })
    @PostMapping("/register")
    public ResponseEntity<UserTO> register(@RequestParam String branch, @RequestBody UserTO branchStaff) throws ConflictRestException {
        try {

            // staff user can not register for the branch is already taken
            if (middlewareUserService.countUsersByBranch(branch) > 0) {
                throw new ForbiddenRestException(USER_CANNOT_REGISTER_IN_BRANCH);
            }

            branchStaff.setBranch(branch);
            branchStaff.setUserRoles(Collections.singletonList(UserRoleTO.STAFF));
            UserTO user = middlewareUserService.create(branchStaff);
            user.setPin(null);

            return ResponseEntity.ok(user);
        } catch (UserAlreadyExistsMiddlewareException e) {
            throw new ConflictRestException(e.getMessage()).withDevMessage(e.getMessage());
        }
    }

    /**
     * Authorize returns a bearer token that can be reused by the consuming application.
     *
     * @param userCredentials tpp login and tpp pin
     * @return JWT token and user info
     */
    @ApiOperation(tags=UnprotectedEndpoint.UNPROTECTED_ENDPOINT, value="Login",
            notes="Initiates the user login process. Returns a login response object describing how to proceed.")
    @ApiResponses(value={
            @ApiResponse(code=200, response=SCALoginResponseTO.class, message="Success. LoginToken contained in the returned response object."),
            @ApiResponse(code=401, message="Wrong authentication credential."),
            @ApiResponse(code=403, message="Authenticated but user does not have the requested role.")
    })
    @PostMapping("/login")
    public ResponseEntity<SCALoginResponseTO> login(@RequestBody UserCredentialsTO userCredentials) throws NotFoundRestException, ForbiddenRestException {
        try {
            return ResponseEntity.ok(onlineBankingService.authorise(userCredentials.getLogin(), userCredentials.getPin(), UserRoleTO.STAFF));
        } catch (UserNotFoundMiddlewareException e) {
            throw new NotFoundRestException(e.getMessage()).withDevMessage(e.getMessage());
        } catch (InsufficientPermissionMiddlewareException e) {
            throw new ForbiddenRestException(e.getMessage()).withDevMessage(e.getMessage());
        }
    }

    /**
     * Creates new user within the same branch
     *
     * @param user user to be created
     * @return created user
     */
    @ApiOperation(value="Create user",
            notes="Create new user with the same branch as creator.",
            authorizations =@Authorization(value="apiKey"))
    @ApiResponses(value={
            @ApiResponse(code=200, response=UserTO.class, message="Success. Created user in provided in the response."),
            @ApiResponse(code=401, message="Wrong authentication credential."),
            @ApiResponse(code=403, message="Authenticated but user does not have the requested role.")
    })
    @PreAuthorize("hasRole('STAFF')")
    @PostMapping
    public ResponseEntity<UserTO> createUser(@RequestBody UserTO user) throws NotFoundRestException, ConflictRestException{
        try {
            UserTO branchStaff = middlewareUserService.findById(accessToken.getSub());

            // set the same branch for the user the staff member that creates it
            user.setBranch(branchStaff.getBranch());

            // Assert that the role is neither system nor technical
            user.getUserRoles().remove(UserRoleTO.SYSTEM);
            user.getUserRoles().remove(UserRoleTO.TECHNICAL);

            UserTO newUser = middlewareUserService.create(user);
            newUser.setPin(null);

            return ResponseEntity.ok(newUser);
        } catch (UserNotFoundMiddlewareException e) {
            throw new NotFoundRestException(e.getMessage());
        } catch (UserAlreadyExistsMiddlewareException e) {
            throw new ConflictRestException(e.getMessage()).withDevMessage(e.getMessage());
        }
    }

    // TODO: pagination for users and limit users for branch
    /**
     * Lists users within the branch and roles
     *
     * @return list of users for the branch with roles
     */
    @ApiOperation(value="Lists users by branch and role",
            notes="Lists users by branch and roles.",
            authorizations =@Authorization(value="apiKey"))
    @ApiResponses(value={
            @ApiResponse(code=200, response=UserTO.class, message="Success. Created user in provided in the response."),
            @ApiResponse(code=401, message="Wrong authentication credential."),
            @ApiResponse(code=403, message="Authenticated but user does not have the requested role.")
    })
    @PreAuthorize("hasRole('STAFF')")
    @GetMapping
    public ResponseEntity<List<UserTO>> getBranchUsersByRoles(@RequestParam List<UserRoleTO> roles) throws NotFoundRestException{
        try {
            UserTO branchStaff = middlewareUserService.findById(accessToken.getSub());
            List<UserTO> users = middlewareUserService.getUsersByBranchAndRoles(branchStaff.getBranch(), roles);
            return ResponseEntity.ok(users);
        } catch (UserNotFoundMiddlewareException e) {
            throw new NotFoundRestException(e.getMessage());
        }
    }

    /**
     * Gets user by ID if it's within the branch
     * @param userId user ID
     * @return user
     */
    @ApiOperation(value="Gets user by ID if it's within the branch",
            notes="Gets user by ID if it's within the branch.",
            authorizations =@Authorization(value="apiKey"))
    @ApiResponses(value={
            @ApiResponse(code=200, response=UserTO.class, message="Success. Created user in provided in the response."),
            @ApiResponse(code=401, message="Wrong authentication credential."),
            @ApiResponse(code=403, message="Authenticated but user does not have the requested role.")
    })
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<UserTO> getBranchUserById(@PathVariable String userId) throws NotFoundRestException {
        try {
            UserTO branchStaff = middlewareUserService.findById(accessToken.getSub());
            UserTO user = middlewareUserService.findById(userId);

            if (!branchStaff.getBranch().equals(user.getBranch())) {
                throw new ForbiddenRestException(USER_NOT_IN_BRANCH);
            }

            return ResponseEntity.ok(user);
        } catch (UserNotFoundMiddlewareException e) {
            throw new NotFoundRestException(e.getMessage());
        }
    }

    /**
     * Updates SCA Data for user if it's within the branch
     * @param userId user ID
     * @param data user SCA data
     * @return updated user
     */
    @ApiOperation(value="Updates SCA Data for user if it's within the branch.",
            notes="Updates SCA Data for user if it's within the branch.",
            authorizations =@Authorization(value="apiKey"))
    @ApiResponses(value={
            @ApiResponse(code=200, response=UserTO.class, message="Success. Created user in provided in the response."),
            @ApiResponse(code=401, message="Wrong authentication credential."),
            @ApiResponse(code=403, message="Authenticated but user does not have the requested role.")
    })
    @PostMapping("/{userId}/sca-data")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<Void> updateUserScaData(@PathVariable String userId, @RequestBody List<ScaUserDataTO> data) {
        try {
            UserTO branchStaff = middlewareUserService.findById(accessToken.getSub());
            UserTO user = middlewareUserService.findById(userId);

            if (!branchStaff.getBranch().equals(user.getBranch())) {
                throw new ForbiddenRestException(USER_NOT_IN_BRANCH);
            }

            UserTO userWithUpdatedSca = middlewareUserService.updateScaData(user.getLogin(), data);
            URI uri = UriComponentsBuilder.fromUriString("/staff-access" + UserMgmtRestAPI.BASE_PATH + "/" + userWithUpdatedSca.getId())
                    .build().toUri();
            return ResponseEntity.created(uri).build();
        } catch (UserNotFoundMiddlewareException e) {
            throw new NotFoundRestException(e.getMessage());
        }
    }

}
