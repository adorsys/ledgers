/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.cleanup.repository;

import de.adorsys.ledgers.cleanup.DepositAccountCleanupRepository;
import de.adorsys.ledgers.cleanup.exception.CleanupModuleException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

import static java.lang.String.format;

@Slf4j
@Service
public class DepositAccountCleanupRepositoryImpl extends BaseCleanupRepository implements DepositAccountCleanupRepository {

    private static final String BRANCH_SQL = "deleteBranch.sql";
    private static final String ROLL_BACK_BRANCH_SQL = "rollBackBranch.sql";
    private static final String POSTING_SQL = "deletePostings.sql";
    private static final String USER_SQL = "deleteUser.sql";
    private static final String ACCOUNT_SQL = "deleteAccount.sql";

    private static final String DELETE_BRANCH_ERROR_MSG = "Something went wrong during deletion of branch: %s, msg: %s";
    private static final String ROLL_BACK_BRANCH_ERROR_MSG = "Something went wrong during rollback of branch: %s, msg: %s";
    private static final String DELETE_POSTINGS_ERROR_MSG = "Something went wrong during deletion of postings for iban: %s, msg: %s";
    private static final String DELETE_USER_ERROR_MSG = "Something went wrong during deletion of user: %s, msg: %s";
    private static final String DELETE_ACCOUNT_ERROR_MSG = "Something went wrong during deletion of account: %s, msg: %s";

    public DepositAccountCleanupRepositoryImpl(EntityManager entityManager, ResourceLoader resourceLoader) {
        super(entityManager, resourceLoader);
    }

    @Override
    public void deleteBranch(String branchId) throws CleanupModuleException {
        try {
            executeUpdate(BRANCH_SQL, Map.of(1, branchId));
        } catch (IOException e) {
            throw CleanupModuleException.builder()
                          .devMsg(format(DELETE_BRANCH_ERROR_MSG, branchId, e.getMessage()))
                          .build();
        }
    }

    @Override
    public void deleteUser(String userId) throws CleanupModuleException {
        try {
            executeUpdate(USER_SQL, Map.of(1, userId));
        } catch (IOException e) {
            throw CleanupModuleException.builder()
                          .devMsg(format(DELETE_USER_ERROR_MSG, userId, e.getMessage()))
                          .build();
        }
    }

    @Override
    public void deleteAccount(String accountId) throws CleanupModuleException {
        try {
            executeUpdate(ACCOUNT_SQL, Map.of(1, accountId));
        } catch (IOException e) {
            throw CleanupModuleException.builder()
                          .devMsg(format(DELETE_ACCOUNT_ERROR_MSG, accountId, e.getMessage()))
                          .build();
        }
    }

    @Override
    public void deletePostings(String id) throws CleanupModuleException {
        try {
            executeUpdate(POSTING_SQL, Map.of(1, id));
        } catch (IOException e) {
            throw CleanupModuleException.builder()
                          .devMsg(format(DELETE_POSTINGS_ERROR_MSG, id, e.getMessage()))
                          .build();
        }
    }

    @Override
    public void rollBackBranch(String branch, LocalDateTime revertTimestamp) throws CleanupModuleException {
        try {
            executeUpdate(ROLL_BACK_BRANCH_SQL, Map.of(1, branch,
                                                       2, revertTimestamp));
        } catch (IOException e) {
            throw CleanupModuleException.builder()
                          .devMsg(format(ROLL_BACK_BRANCH_ERROR_MSG, branch, e.getMessage()))
                          .build();
        }
    }
}
