/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.sca.db.repository;

import de.adorsys.ledgers.sca.db.domain.RecoveryPointEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface RecoveryPointRepository extends CrudRepository<RecoveryPointEntity, String> {
    List<RecoveryPointEntity> findAllByBranchId(String branchId);

    Optional<RecoveryPointEntity> findByIdAndBranchId(long id, String branchId);

    void deleteById(long id);

    boolean existsByIdAndBranchId(long id, String branchId);
}
