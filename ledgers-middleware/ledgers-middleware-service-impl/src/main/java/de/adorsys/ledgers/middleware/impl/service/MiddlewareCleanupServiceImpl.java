/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.deposit.api.service.DepositAccountCleanupService;
import de.adorsys.ledgers.keycloak.client.api.KeycloakDataService;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.service.MiddlewareCleanupService;
import de.adorsys.ledgers.um.api.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MiddlewareCleanupServiceImpl implements MiddlewareCleanupService {

    private static final int NANO_TO_SECOND = 1000000000;

    private final UserService userService;
    private final DepositAccountCleanupService depositAccountCleanupService;
    private final KeycloakDataService keycloakDataService;

    @Override
    public void deleteTransactions(String userId, UserRoleTO userRole, String accountId) {
        log.info("User {} attempting delete postings for account: {}", userId, accountId);
        long start = System.nanoTime();
        depositAccountCleanupService.deleteTransactions(accountId);
        log.info("Deleting postings for account: {} Successful, in {} seconds", accountId, (double) (System.nanoTime() - start) / NANO_TO_SECOND);
    }

    @Override
    public void deleteAccount(String userId, UserRoleTO userRole, String accountId) {
        log.info("User {} attempting delete account: {}", userId, accountId);
        long start = System.nanoTime();
        depositAccountCleanupService.deleteAccount(accountId);
        log.info("Deleting account: {} Successful, in {} seconds", accountId, (double) (System.nanoTime() - start) / NANO_TO_SECOND);
    }

    @Override
    public void deleteUser(String userId, UserRoleTO userRole, String userToDeleteId) {
        log.info("User {} attempting delete user: {}", userId, userToDeleteId);
        long start = System.nanoTime();
        String login = userService.findById(userToDeleteId).getLogin();
        depositAccountCleanupService.deleteUser(userToDeleteId);
        keycloakDataService.deleteUser(login);
        log.info("Deleting user: {} Successful, in {} seconds", userToDeleteId, (double) (System.nanoTime() - start) / NANO_TO_SECOND);
    }

    @Override
    @Transactional
    public void removeBranch(String userId, UserRoleTO userRole, String branchId) {
        log.info("User {} attempting delete branch {}", userId, branchId);
        long start = System.nanoTime();

        // Remove data in Keycloak.
        userService.findUserLoginsByBranch(branchId)
                .forEach(keycloakDataService::deleteUser);

        // Remove data in Ledgers.
        depositAccountCleanupService.deleteBranch(branchId);

        log.info("Deleting branch {} Successful, in {} seconds", branchId, TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - start));
    }
}
