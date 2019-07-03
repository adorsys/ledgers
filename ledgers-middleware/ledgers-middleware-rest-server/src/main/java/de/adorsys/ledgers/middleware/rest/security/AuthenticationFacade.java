package de.adorsys.ledgers.middleware.rest.security;

import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;

public interface AuthenticationFacade {

    String getUserId();

    ScaInfoTO getScaInfoWithScaMethodId(String scaMethodId);

    ScaInfoTO getScaInfoWithAuthCode(String authCode);
}
