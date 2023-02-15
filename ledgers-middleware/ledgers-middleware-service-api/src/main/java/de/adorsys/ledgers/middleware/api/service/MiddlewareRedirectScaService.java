/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.api.service;

import de.adorsys.ledgers.middleware.api.domain.sca.GlobalScaResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.sca.StartScaOprTO;

public interface MiddlewareRedirectScaService {
    GlobalScaResponseTO startScaOperation(StartScaOprTO scaOpr, ScaInfoTO scaInfo);

    GlobalScaResponseTO getMethods(String authorizationId, ScaInfoTO scaInfo);

    GlobalScaResponseTO selectMethod(ScaInfoTO scaInfo);

    GlobalScaResponseTO confirmAuthorization(ScaInfoTO scaInfo);

    StartScaOprTO loadScaInformation(String authorizationId);
}
