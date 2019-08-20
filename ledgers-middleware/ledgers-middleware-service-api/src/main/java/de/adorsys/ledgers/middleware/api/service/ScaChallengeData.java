package de.adorsys.ledgers.middleware.api.service;

import de.adorsys.ledgers.middleware.api.domain.sca.ChallengeDataTO;
import de.adorsys.ledgers.middleware.api.domain.um.ScaMethodTypeTO;

public interface ScaChallengeData {
    ChallengeDataTO getChallengeData(String methodValue);

    ScaMethodTypeTO getScaMethodType();
}
