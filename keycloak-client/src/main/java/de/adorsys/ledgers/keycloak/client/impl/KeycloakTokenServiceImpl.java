package de.adorsys.ledgers.keycloak.client.impl;

import de.adorsys.ledgers.keycloak.client.api.KeycloakTokenService;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;

public class KeycloakTokenServiceImpl implements KeycloakTokenService {

    @Override
    public BearerTokenTO login(String login, String password) {
        return null;
    }

    @Override
    public BearerTokenTO exchangeToken(BearerTokenTO oldToken) {
        return null;
    }

    @Override
    public BearerTokenTO validateToken(BearerTokenTO token) {
        return null;
    }
}
