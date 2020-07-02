package de.adorsys.ledgers.middleware.api.service;


import de.adorsys.ledgers.middleware.api.domain.sca.GlobalScaResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaLoginOprTO;

public interface MiddlewareScaService {
    GlobalScaResponseTO loginForOperation(ScaLoginOprTO loginOpr);

    GlobalScaResponseTO getMethods(String authorizationId, String userId);

    GlobalScaResponseTO selectMethod(ScaInfoTO scaInfo);

    GlobalScaResponseTO confirmAuthorization(ScaInfoTO scaInfo);
}
