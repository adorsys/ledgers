/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.deposit.api.service.DepositAccountCleanupService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.keycloak.client.api.KeycloakDataService;
import de.adorsys.ledgers.middleware.api.domain.general.RecoveryPointTO;
import de.adorsys.ledgers.middleware.api.service.MiddlewareRecoveryService;
import de.adorsys.ledgers.middleware.impl.converter.RecoveryPointMapperTO;
import de.adorsys.ledgers.sca.domain.RecoveryPointBO;
import de.adorsys.ledgers.sca.service.RecoveryPointService;
import de.adorsys.ledgers.um.api.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MiddlewareRecoveryPointServiceImpl implements MiddlewareRecoveryService {
    private static final int NANO_TO_SECOND = 1000000000;
    private static final ExecutorService FIXED_THREAD_POOL = Executors.newFixedThreadPool(20);

    private final RecoveryPointService pointService;
    private final RecoveryPointMapperTO mapper;
    private final UserService userService;
    private final DepositAccountService depositAccountService;
    private final DepositAccountCleanupService depositAccountCleanupService;
    private final KeycloakDataService keycloakDataService;

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

    @Override
    @Transactional
    public void revertDatabase(String userId, long recoveryPointId) {
        // First, all users for this branch should be technically blocked.
        long start = System.nanoTime();
        log.info("Started reverting state for {}", userId);
        RecoveryPointTO point = getPointById(userId, recoveryPointId);

        systemBlockBranch(userId, true);
        log.info("All branch data is LOCKED in {} seconds", (double) (System.nanoTime() - start) / NANO_TO_SECOND);

        // Delete data in Keycloak.
        userService.findUsersByBranchAndCreatedAfter(userId, point.getRollBackTime())
                .forEach(user -> keycloakDataService.deleteUser(user.getLogin()));

        // Delete data in Ledgers.
        depositAccountCleanupService.rollBackBranch(userId, point.getRollBackTime());
        systemBlockBranch(userId, false);
        log.info("Reverted data and unlocked branch in {}s", (double) (System.nanoTime() - start) / NANO_TO_SECOND);
    }

    private void systemBlockBranch(String branchId, boolean statusToSet) {
        CompletableFuture.runAsync(() -> userService.setBranchBlockedStatus(branchId, true, statusToSet), FIXED_THREAD_POOL)
                .thenRunAsync(() -> depositAccountService.changeAccountsBlockedStatus(branchId, true, statusToSet));
    }
}
