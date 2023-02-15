/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.deposit.api.service;

import java.time.LocalDateTime;

public interface DepositAccountCleanupService {

    void deleteTransactions(String accountId);

    void deleteBranch(String branchId);

    void deleteUser(String userId);

    void deleteAccount(String accountId);

    void rollBackBranch(String branch, LocalDateTime revertTimestamp);
}
