/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.api.service;

import de.adorsys.ledgers.middleware.api.domain.general.RecoveryPointTO;

import java.util.List;

public interface MiddlewareRecoveryService {
    void createRecoveryPoint(String branchId, RecoveryPointTO recoveryPoint);

    List<RecoveryPointTO> getAll(String branchId);

    RecoveryPointTO getPointById(String branchId, Long id);

    void deleteById(String branchId, Long id);

    void revertDatabase(String userId, long recoveryPointId);
}
