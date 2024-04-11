/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.deposit.api.service.impl;

import de.adorsys.ledgers.cleanup.DepositAccountCleanupRepository;
import de.adorsys.ledgers.cleanup.exception.CleanupModuleException;
import de.adorsys.ledgers.deposit.api.domain.DepositAccountBO;
import de.adorsys.ledgers.deposit.api.service.DepositAccountCleanupService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.postings.api.domain.LedgerAccountBO;
import de.adorsys.ledgers.postings.api.service.LedgerService;
import de.adorsys.ledgers.util.exception.DepositModuleException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static de.adorsys.ledgers.util.exception.DepositErrorCode.COULD_NOT_EXECUTE_STATEMENT;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepositAccountCleanupServiceImpl implements DepositAccountCleanupService {

    private final DepositAccountService depositAccountService;
    private final DepositAccountCleanupRepository depositAccountCleanupRepository;
    private final LedgerService ledgerService;

    @Override
    public void deleteTransactions(String accountId) {
        DepositAccountBO account = depositAccountService.getAccountById(accountId);
        String linked = account.getLinkedAccounts();
        LedgerAccountBO ledgerAccount = ledgerService.findLedgerAccountById(linked);
        try {
            depositAccountCleanupRepository.deletePostings(ledgerAccount.getId());
        } catch (CleanupModuleException e) {
            throw DepositModuleException.builder()
                          .devMsg(e.getDevMsg())
                          .errorCode(COULD_NOT_EXECUTE_STATEMENT)
                          .build();
        }
    }

    @Override
    public void deleteBranch(String branchId) {
        try {
            depositAccountCleanupRepository.deleteBranch(branchId);
        } catch (CleanupModuleException e) {
            throw DepositModuleException.builder()
                          .devMsg(e.getDevMsg())
                          .errorCode(COULD_NOT_EXECUTE_STATEMENT)
                          .build();
        }
    }

    @Override
    public void deleteUser(String userId) {
        try {
            depositAccountCleanupRepository.deleteUser(userId);
        } catch (CleanupModuleException e) {
            throw DepositModuleException.builder()
                          .devMsg(e.getDevMsg())
                          .errorCode(COULD_NOT_EXECUTE_STATEMENT)
                          .build();
        }
    }

    @Override
    public void deleteAccount(String accountId) {
        try {
            depositAccountCleanupRepository.deleteAccount(accountId);
        } catch (CleanupModuleException e) {
            throw DepositModuleException.builder()
                          .devMsg(e.getDevMsg())
                          .errorCode(COULD_NOT_EXECUTE_STATEMENT)
                          .build();
        }
    }

    @Override
    public void rollBackBranch(String branch, LocalDateTime revertTimestamp) {
        try {
            depositAccountCleanupRepository.rollBackBranch(branch, revertTimestamp);
        } catch (CleanupModuleException e) {
            throw DepositModuleException.builder()
                          .devMsg(e.getDevMsg())
                          .errorCode(COULD_NOT_EXECUTE_STATEMENT)
                          .build();
        }
    }
}
