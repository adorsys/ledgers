package de.adorsys.ledgers.middleware.rest.resource;

import de.adorsys.ledgers.middleware.api.domain.sca.SCALoginResponseTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.exception.UserAlreadyExistsMiddlewareException;
import de.adorsys.ledgers.middleware.api.service.MiddlewareOnlineBankingService;
import de.adorsys.ledgers.middleware.rest.annotation.MiddlewareUserResource;
import de.adorsys.ledgers.middleware.rest.exception.ConflictRestException;
import de.adorsys.ledgers.middleware.rest.exception.ForbiddenRestException;
import de.adorsys.ledgers.middleware.rest.exception.NotFoundRestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(TppRestAPI.BASE_PATH)
@MiddlewareUserResource
public class TppResource implements TppRestAPI {

    private final MiddlewareOnlineBankingService onlineBankingService;

    public TppResource(MiddlewareOnlineBankingService onlineBankingService) {
        super();
        this.onlineBankingService = onlineBankingService;
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
    public ResponseEntity<SCALoginResponseTO> login(UserTO userCredentials) throws NotFoundRestException, ForbiddenRestException {
        return null;
    }

}
