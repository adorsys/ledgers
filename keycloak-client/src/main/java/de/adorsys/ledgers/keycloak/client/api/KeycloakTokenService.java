package de.adorsys.ledgers.keycloak.client.api;

import de.adorsys.ledgers.um.api.domain.BearerTokenBO;

public interface KeycloakTokenService {

    BearerTokenBO login(String login, String password);

    BearerTokenBO exchangeToken(String oldToken, Integer timeToLive);

    boolean validate(BearerTokenBO token);
}
