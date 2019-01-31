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
import java.util.List;


@RestController
@RequestMapping(TppRestAPI.BASE_PATH)
@MiddlewareUserResource
public class TppResource implements TppRestAPI {

    private final MiddlewareOnlineBankingService onlineBankingService;
    private final MiddlewareUserManagementService middlewareUserService;
    private final AccessTokenTO accessToken;

    public TppResource(
            MiddlewareOnlineBankingService onlineBankingService,
            MiddlewareUserManagementService middlewareUserService,
            AccessTokenTO accessToken) {
        super();
        this.onlineBankingService = onlineBankingService;
        this.middlewareUserService = middlewareUserService;
        this.accessToken = accessToken;
    }

    @Override
    public ResponseEntity<UserTO> register(UserTO tpp) throws ConflictRestException {
        try {
            UserTO user = onlineBankingService.register(tpp.getLogin(), tpp.getEmail(), tpp.getPin(), UserRoleTO.TECHNICAL);
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
    @PreAuthorize("hasRole('TECHNICAL')")
    public ResponseEntity<UserTO> createUser(UserTO user) throws NotFoundRestException, ConflictRestException{
        try {
            UserTO tpp = middlewareUserService.findById(accessToken.getSub());

            // TODO: add parent user to user entity
            UserTO newUser = middlewareUserService.create(user);

            tpp.getCreatedUsers().add(newUser);

            return ResponseEntity.ok(user);
        } catch (UserNotFoundMiddlewareException e) {
            throw new NotFoundRestException(e.getMessage());
        } catch (UserAlreadyExistsMiddlewareException e) {
            throw new ConflictRestException(e.getMessage()).withDevMessage(e.getMessage());
        }
    }

    // TODO: pagination for users and limit users for TPP
    @Override
    @PreAuthorize("tokenUsage('DIRECT_ACCESS')")
    public ResponseEntity<List<UserTO>> getTppUsers()throws NotFoundRestException{
        try {
            UserTO tpp = middlewareUserService.findById(accessToken.getSub());

            return ResponseEntity.ok(tpp.getCreatedUsers());
        } catch (UserNotFoundMiddlewareException e) {
            throw new NotFoundRestException(e.getMessage());
        }
    }

    @Override
    @PreAuthorize("tokenUsage('DIRECT_ACCESS')")
    public ResponseEntity<UserTO> getTppUser(String userId) throws NotFoundRestException {
        try {
            UserTO tpp = middlewareUserService.findById(accessToken.getSub());

            // TODO: check if tpp has this user
            UserTO user = middlewareUserService.findById(userId);

            return ResponseEntity.ok(user);
        } catch (UserNotFoundMiddlewareException e) {
            throw new NotFoundRestException(e.getMessage());
        }
    }

    @Override
    @PreAuthorize("tokenUsage('DIRECT_ACCESS')")
    public ResponseEntity<Void> updateUserScaData(String userId, List<ScaUserDataTO> data) {
        try {
            UserTO tpp = middlewareUserService.findById(accessToken.getSub());

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
