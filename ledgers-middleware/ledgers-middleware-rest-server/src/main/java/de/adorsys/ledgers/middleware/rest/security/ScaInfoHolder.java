package de.adorsys.ledgers.middleware.rest.security;

import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.um.TokenUsageTO;

public interface ScaInfoHolder {

    String getUserId();

    TokenUsageTO getTokenUsageTO();

    ScaInfoTO getScaInfo();

    ScaInfoTO getScaInfoWithScaMethodIdAndAuthorisationId(String scaMethodId, String authorizationId);

    ScaInfoTO getScaInfoWithAuthCode(String authCode);
}
