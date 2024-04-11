/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.um.api.service;

import de.adorsys.ledgers.um.api.domain.ScaUserDataBO;

import java.util.List;

public interface ScaUserDataService {

    ScaUserDataBO findByEmail(String email);

    ScaUserDataBO findById(String scaId);

    void updateScaUserData(ScaUserDataBO scaUserDataBO);

    void ifScaChangedEmailNotValid(List<ScaUserDataBO> oldScaData, List<ScaUserDataBO> newScaData);
}