/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.api.service;

import de.adorsys.ledgers.middleware.api.domain.sca.ChallengeDataTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaDataInfoTO;
import de.adorsys.ledgers.middleware.api.domain.um.ScaMethodTypeTO;

public interface ScaChallengeData {
    ChallengeDataTO getChallengeData(ScaDataInfoTO scaDataInfoTO);

    ScaMethodTypeTO getScaMethodType();
}
