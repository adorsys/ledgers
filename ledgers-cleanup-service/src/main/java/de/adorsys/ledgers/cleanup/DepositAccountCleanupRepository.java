package de.adorsys.ledgers.cleanup;

import de.adorsys.ledgers.cleanup.exception.CleanupModuleException;

import java.time.LocalDateTime;

public interface DepositAccountCleanupRepository {

    void deleteBranch(String branchId) throws CleanupModuleException;

    void deleteUser(String userId) throws CleanupModuleException;

    void deleteAccount(String accountId) throws CleanupModuleException;

    void deletePostings(String id) throws CleanupModuleException;

    void rollBackBranch(String branch, LocalDateTime revertTimestamp) throws CleanupModuleException;
}
