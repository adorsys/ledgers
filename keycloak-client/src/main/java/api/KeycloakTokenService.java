package api;

import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;

public interface KeycloakTokenService {

    BearerTokenTO login(String login, String password);

    BearerTokenTO exchangeToken(BearerTokenTO oldToken);

    BearerTokenTO validateToken(BearerTokenTO token);
}
