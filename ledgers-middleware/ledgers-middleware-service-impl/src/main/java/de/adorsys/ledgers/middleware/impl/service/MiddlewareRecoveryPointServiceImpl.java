package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.middleware.api.domain.general.RecoveryPointTO;
import de.adorsys.ledgers.middleware.api.service.MiddlewareRecoveryService;
import de.adorsys.ledgers.middleware.impl.converter.RecoveryPointMapperTO;
import de.adorsys.ledgers.sca.domain.RecoveryPointBO;
import de.adorsys.ledgers.sca.service.RecoveryPointService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MiddlewareRecoveryPointServiceImpl implements MiddlewareRecoveryService {
    private final RecoveryPointService pointService;
    private final RecoveryPointMapperTO mapper;

    @Override
    public void createRecoveryPoint(String branchId, RecoveryPointTO recoveryPoint) {
        RecoveryPointBO recoveryPointBO = mapper.toBO(recoveryPoint);
        recoveryPointBO.setBranchId(branchId);
        pointService.createRecoveryPoint(recoveryPointBO);
    }

    @Override
    public List<RecoveryPointTO> getAll(String branchId) {
        return mapper.toTOs(pointService.getAllByBranch(branchId));
    }

    @Override
    public RecoveryPointTO getPointById(String branchId, Long id) {
        return mapper.toTO(pointService.getById(id, branchId));
    }

    @Override
    public void deleteById(String branchId, Long id) {
        pointService.deleteRecoveryPoint(id, branchId);
    }
}
