/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.sca.service;

import de.adorsys.ledgers.sca.domain.RecoveryPointBO;

import java.util.List;

public interface RecoveryPointService {
    RecoveryPointBO getById(long id, String branchId);

    List<RecoveryPointBO> getAllByBranch(String branchId);

    void deleteRecoveryPoint(long id, String branchId);

    void createRecoveryPoint(RecoveryPointBO recoveryPoint);
}