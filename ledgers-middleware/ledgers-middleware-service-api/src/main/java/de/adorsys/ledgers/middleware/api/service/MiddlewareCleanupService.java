package de.adorsys.ledgers.middleware.api.service;

import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;

public interface MiddlewareCleanupService {

    /**
     * Remove all transactions for deposit account
     *
     * @param userId    id of the user
     * @param userRole  role of user initiating operation
     * @param accountId the account id
     */
    void deleteTransactions(String userId, UserRoleTO userRole, String accountId);

    void deleteAccount(String userId, UserRoleTO userRole, String accountId);

    void deleteUser(String userId, UserRoleTO userRole, String userToDeleteId);

    /**
     * @param userId   id of user initiating operation
     * @param role     role of user initiating operation
     * @param branchId branch to remove
     */
    void removeBranch(String userId, UserRoleTO role, String branchId);
}
