/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.sca.db.repository;

import java.util.List;
import java.util.Optional;

import de.adorsys.ledgers.sca.db.domain.ScaStatus;
import org.springframework.data.repository.CrudRepository;

import de.adorsys.ledgers.sca.db.domain.AuthCodeStatus;
import de.adorsys.ledgers.sca.db.domain.OpType;
import de.adorsys.ledgers.sca.db.domain.SCAOperationEntity;

public interface SCAOperationRepository extends CrudRepository<SCAOperationEntity, String> {

    List<SCAOperationEntity> findByStatus(AuthCodeStatus status);

    List<SCAOperationEntity> findByOpIdAndOpType(String opId, OpType opType);

    Optional<SCAOperationEntity> findByIdAndScaStatus(String authorisationId, ScaStatus scaStatus);
}
