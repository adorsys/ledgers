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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.List;


@RestController
@RequestMapping(BranchRestApi.BASE_PATH)
@MiddlewareUserResource
public class BranchResource implements BranchRestApi {

    private final MiddlewareOnlineBankingService onlineBankingService;
    private final MiddlewareUserManagementService middlewareUserService;
    private final AccessTokenTO accessToken;
    private static final String USER_NOT_IN_BRANCH = "User is not your branch";

    public BranchResource(
            MiddlewareOnlineBankingService onlineBankingService,
            MiddlewareUserManagementService middlewareUserService,
            AccessTokenTO accessToken) {
        super();
        this.onlineBankingService = onlineBankingService;
        this.middlewareUserService = middlewareUserService;
        this.accessToken = accessToken;
    }

    @Override
    public ResponseEntity<UserTO> register(String branch, UserTO branchStaff) throws ConflictRestException {
        try {
//            if (middlewareUserService.countByBranch(branch)>0) {
//                // error branch exist
//            };
            branchStaff.setBranch(branch);
            branchStaff.setUserRoles(Collections.singletonList(UserRoleTO.STAFF));
            UserTO user = middlewareUserService.create(branchStaff);
            user.setPin(null);

            return ResponseEntity.ok(user);
        } catch (UserAlreadyExistsMiddlewareException e) {
            throw new ConflictRestException(e.getMessage()).withDevMessage(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<SCALoginResponseTO> login(UserCredentialsTO userCredentials) throws NotFoundRestException, ForbiddenRestException {
        try {
            return ResponseEntity.ok(onlineBankingService.authorise(userCredentials.getLogin(), userCredentials.getPin(), UserRoleTO.STAFF));
        } catch (UserNotFoundMiddlewareException e) {
            throw new NotFoundRestException(e.getMessage()).withDevMessage(e.getMessage());
        } catch (InsufficientPermissionMiddlewareException e) {
            throw new ForbiddenRestException(e.getMessage()).withDevMessage(e.getMessage());
        }
    }

    @Override
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<UserTO> createUser(UserTO user) throws NotFoundRestException, ConflictRestException{
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
    @Override
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<List<UserTO>> getBranchUsersByRoles(List<UserRoleTO> roles) throws NotFoundRestException{
        try {
            UserTO branchStaff = middlewareUserService.findById(accessToken.getSub());
            List<UserTO> users = middlewareUserService.getUsersByBranchAndRoles(branchStaff.getBranch(), roles);
            return ResponseEntity.ok(users);
        } catch (UserNotFoundMiddlewareException e) {
            throw new NotFoundRestException(e.getMessage());
        }
    }

    @Override
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<UserTO> getBranchUserById(String userId) throws NotFoundRestException {
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

    @Override
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<Void> updateUserScaData(String userId, List<ScaUserDataTO> data) {
        try {
            UserTO branchStaff = middlewareUserService.findById(accessToken.getSub());
            UserTO user = middlewareUserService.findById(userId);

            if (!branchStaff.getBranch().equals(user.getBranch())) {
                throw new ForbiddenRestException(USER_NOT_IN_BRANCH);
            }

            UserTO userWithUpdatedSca = middlewareUserService.updateScaData(user.getLogin(), data);
            URI uri = UriComponentsBuilder.fromUriString(BASE_PATH + "/" + userWithUpdatedSca.getId())
                    .build().toUri();
            return ResponseEntity.created(uri).build();
        } catch (UserNotFoundMiddlewareException e) {
            throw new NotFoundRestException(e.getMessage());
        }
    }

}
