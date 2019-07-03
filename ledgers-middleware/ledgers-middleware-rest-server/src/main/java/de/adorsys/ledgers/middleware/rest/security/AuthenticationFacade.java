package de.adorsys.ledgers.middleware.rest.security;

import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.um.TokenUsageTO;

public interface AuthenticationFacade {

    String getUserId();

    TokenUsageTO getTokenUsageTO();

    ScaInfoTO getScaInfo();

    ScaInfoTO getScaInfoWithScaMethodId(String scaMethodId);

    ScaInfoTO getScaInfoWithAuthCode(String authCode);
}
