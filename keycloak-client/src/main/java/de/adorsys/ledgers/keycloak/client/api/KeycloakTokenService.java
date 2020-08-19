package de.adorsys.ledgers.keycloak.client.api;

import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;

public interface KeycloakTokenService {

    BearerTokenTO login(String login, String password);

    BearerTokenTO exchangeToken(BearerTokenTO oldToken);

    boolean validate(BearerTokenTO token);
}
