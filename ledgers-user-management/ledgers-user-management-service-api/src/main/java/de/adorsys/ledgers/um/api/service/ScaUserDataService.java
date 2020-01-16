package de.adorsys.ledgers.um.api.service;

import de.adorsys.ledgers.um.api.domain.ScaUserDataBO;

public interface ScaUserDataService {

    ScaUserDataBO findByEmail(String email);

    ScaUserDataBO findById(String scaId);

    void updateScaUserData(ScaUserDataBO scaUserDataBO);
}
