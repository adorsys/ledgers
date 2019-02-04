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

    public BranchResource(
            MiddlewareOnlineBankingService onlineBankingService,
            MiddlewareUserManagementService middlewareUserService,
            AccessTokenTO accessToken) {
        super();
        this.onlineBankingService = onlineBankingService;
        this.middlewareUserService = middlewareUserService;
        this.accessToken = accessToken;
    }

    // TODO:
    @Override
    public ResponseEntity<UserTO> register(String branch, UserTO branchStaff) throws ConflictRestException {
        try {
//            if (middlewareUserService.countByBranch(branch)>0) {
//                // error branch exist
//            };
            branchStaff.setBranch(branch);
            branchStaff.setUserRoles(Collections.singletonList(UserRoleTO.STAFF));
            UserTO user = middlewareUserService.create(branchStaff);

//            UserTO user = onlineBankingService.register(branchStaff.getLogin(), branchStaff.getEmail(), branchStaff.getPin(), UserRoleTO.STAFF);''
            user.setPin(null);
            return ResponseEntity.ok(user);
        } catch (UserAlreadyExistsMiddlewareException e) {
            throw new ConflictRestException(e.getMessage()).withDevMessage(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<SCALoginResponseTO> login(UserCredentialsTO userCredentials) throws NotFoundRestException, ForbiddenRestException {
        try {
            return ResponseEntity.ok(onlineBankingService.authorise(userCredentials.getLogin(), userCredentials.getPin(), UserRoleTO.TECHNICAL));
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
            UserTO tpp = middlewareUserService.findById(accessToken.getSub());

            user.setBranch(tpp.getBranch());

            // Make sure no system or technical
            user.getUserRoles().remove(UserRoleTO.SYSTEM);
            user.getUserRoles().remove(UserRoleTO.TECHNICAL);

            // TODO: add parent user to user entity
            UserTO newUser = middlewareUserService.create(user);
            newUser.setPin(null);

//            tpp.getCreatedUsers().add(newUser);

            return ResponseEntity.ok(newUser);
        } catch (UserNotFoundMiddlewareException e) {
            throw new NotFoundRestException(e.getMessage());
        } catch (UserAlreadyExistsMiddlewareException e) {
            throw new ConflictRestException(e.getMessage()).withDevMessage(e.getMessage());
        }
    }

    // TODO: pagination for users and limit users for TPP
    @Override
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<List<UserTO>> getBranchUsersByRoles(List<UserRoleTO> roles) throws NotFoundRestException{
        try {
            UserTO staffUser = middlewareUserService.findById(accessToken.getSub());
//            middlewareUserService.findByBranchAndUserRoleIn(tpp.getBranch(), UserRoleTO.CUSTOMER);
            return ResponseEntity.ok(null);


        } catch (UserNotFoundMiddlewareException e) {
            throw new NotFoundRestException(e.getMessage());
        }
    }

    @Override
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<UserTO> getBranchUserById(String userId) throws NotFoundRestException {
        try {
            UserTO staffUser = middlewareUserService.findById(accessToken.getSub());

            // TODO: check if tpp has this user
            UserTO user = middlewareUserService.findById(userId);

            return ResponseEntity.ok(user);
        } catch (UserNotFoundMiddlewareException e) {
            throw new NotFoundRestException(e.getMessage());
        }
    }

    @Override
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<Void> updateUserScaData(String userId, List<ScaUserDataTO> data) {
        try {
            UserTO staffUser = middlewareUserService.findById(accessToken.getSub());

            UserTO user = middlewareUserService.findById(userId);
            UserTO userWithUpdatedSca = middlewareUserService.updateScaData(user.getLogin(), data);

            URI uri = UriComponentsBuilder.fromUriString(BASE_PATH + "/" + userWithUpdatedSca.getId())
                    .build().toUri();

            return ResponseEntity.created(uri).build();
        } catch (UserNotFoundMiddlewareException e) {
            throw new NotFoundRestException(e.getMessage());
        }
    }

}
