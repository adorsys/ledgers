/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.sca.service.impl;

import de.adorsys.ledgers.sca.db.repository.RecoveryPointRepository;
import de.adorsys.ledgers.sca.domain.RecoveryPointBO;
import de.adorsys.ledgers.sca.service.RecoveryPointService;
import de.adorsys.ledgers.sca.service.impl.mapper.RecoveryPointMapper;
import de.adorsys.ledgers.util.exception.SCAErrorCode;
import de.adorsys.ledgers.util.exception.ScaModuleException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecoveryPointServiceImpl implements RecoveryPointService {
    private final RecoveryPointRepository repository;
    private final RecoveryPointMapper mapper;

    @Override
    public RecoveryPointBO getById(long id, String branchId) {
        return repository.findByIdAndBranchId(id, branchId)
                       .map(mapper::toBO)
                       .orElseThrow(() -> ScaModuleException.builder()
                                                  .errorCode(SCAErrorCode.RECOVERY_POINT_NOT_FOUND)
                                                  .devMsg(String.format("Recovery point %s not found", id))
                                                  .build());
    }

    @Override
    public List<RecoveryPointBO> getAllByBranch(String branchId) {
        return mapper.toBOs(repository.findAllByBranchId(branchId));
    }

    @Override
    @Transactional
    public void deleteRecoveryPoint(long id, String branchId) {
        if (!repository.existsByIdAndBranchId(id, branchId)) {
            throw ScaModuleException.builder()
                          .errorCode(SCAErrorCode.RECOVERY_POINT_NOT_FOUND)
                          .devMsg(String.format("Recovery point %s not found", id))
                          .build();
        }
        repository.deleteById(id);
    }

    @Override
    public void createRecoveryPoint(RecoveryPointBO recoveryPoint) {
        recoveryPoint.checkAndUpdateDescription();
        repository.save(mapper.toEntity(recoveryPoint));
    }
}
