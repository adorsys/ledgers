package de.adorsys.ledgers.middleware.rest.resource;

import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.rest.annotation.MiddlewareTppResource;
import de.adorsys.ledgers.middleware.rest.exception.ConflictRestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(TppRestAPI.BASE_PATH)
@MiddlewareTppResource
public class TppResource implements TppRestAPI {

    @Override
    public ResponseEntity<UserTO> register(UserTO tpp) throws ConflictRestException {
        return null;
    }
}
