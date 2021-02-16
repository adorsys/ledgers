package de.adorsys.ledgers.middleware.api.service;

import de.adorsys.ledgers.middleware.api.domain.sca.GlobalScaResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.OpTypeTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;

public interface OperationService {

    <T> GlobalScaResponseTO resolveInitiation(OpTypeTO opType, String opId, T object, ScaInfoTO scaInfo);

    GlobalScaResponseTO execute(OpTypeTO opType, String opId, ScaInfoTO scaInfo);
}
