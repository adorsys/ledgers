package de.adorsys.ledgers.middleware.rest.security;

import de.adorsys.ledgers.middleware.api.domain.um.AccessTokenTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticationFacadeImpl implements AuthenticationFacade {
    private final AccessTokenTO accessTokenTO;

    @Override
    public String getUserId() {
        return accessTokenTO.getSub();
    }
}
