package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.deposit.api.service.DepositAccountInitService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.um.UploadedDataTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareModuleException;
import de.adorsys.ledgers.middleware.api.service.AppManagementService;
import de.adorsys.ledgers.middleware.impl.service.upload.UploadBalanceService;
import de.adorsys.ledgers.middleware.impl.service.upload.UploadDepositAccountService;
import de.adorsys.ledgers.middleware.impl.service.upload.UploadPaymentService;
import de.adorsys.ledgers.middleware.impl.service.upload.UploadUserService;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.domain.UserRoleBO;
import de.adorsys.ledgers.um.api.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO.STAFF;
import static de.adorsys.ledgers.middleware.api.exception.MiddlewareErrorCode.BRANCH_NOT_FOUND;
import static de.adorsys.ledgers.middleware.api.exception.MiddlewareErrorCode.INSUFFICIENT_PERMISSION;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppManagementServiceImpl implements AppManagementService {
    private static final ExecutorService FIXED_THREAD_POOL = Executors.newFixedThreadPool(20);

    private final DepositAccountInitService depositAccountInitService;
    private final UserService userService;
    private final DepositAccountService depositAccountService;
    private final UploadUserService uploadUserService;
    private final UploadDepositAccountService uploadDepositAccountService;
    private final UploadBalanceService uploadBalanceService;
    private final UploadPaymentService uploadPaymentService;

    @Override
    @Transactional
    public void initApp() {
        // Init deposit account config  data.
        log.info("Initiating Ledgers");
        long start = System.nanoTime();
        depositAccountInitService.initConfigData();
        log.info("Initiation completed in {} seconds", TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - start));
    }

    @Override
    @Transactional
    public void removeBranch(String userId, UserRoleTO userRole, String branchId) {
        log.info("User {} attempting delete branch {}", userId, branchId);
        long start = System.nanoTime();
        isExistingBranch(branchId);
        isPermittedToRemoveBranch(userId, userRole, branchId);
        log.info("Permission checked -> OK");
        depositAccountService.deleteBranch(branchId);
        log.info("Deleting branch {} Successful, in {} seconds", branchId, TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - start));
    }

    @Override
    public void uploadData(UploadedDataTO data, ScaInfoTO info) {
        String branchId = userService.findById(info.getUserId()).getBranch();
        List<UserTO> uploadedUsers = uploadUserService.uploadUsers(data.getUsers(), branchId);
        uploadDepositAccountService.uploadDepositAccounts(uploadedUsers, data.getDetails(), info);
        CompletableFuture.runAsync(() -> uploadBalanceService.uploadBalances(data, info), FIXED_THREAD_POOL)
                .thenRunAsync(() -> uploadPaymentService.uploadPayments(data, info));
    }

    @Override
    public boolean changeBlockedStatus(String userId, boolean isSystemBlock) {
        UserBO branch = userService.findById(userId);
        if (!branch.getUserRoles().contains(UserRoleBO.STAFF)) {
            throw MiddlewareModuleException.builder()
                          .devMsg("You're trying to block a user which is not STAFF!")
                          .errorCode(INSUFFICIENT_PERMISSION)
                          .build();
        }
        boolean lockStatusToSet = isSystemBlock ? !branch.isSystemBlocked() : !branch.isBlocked();
        CompletableFuture.runAsync(() -> userService.setBranchBlockedStatus(userId, isSystemBlock, lockStatusToSet), FIXED_THREAD_POOL)
                .thenRunAsync(() -> depositAccountService.changeAccountsBlockedStatus(userId, isSystemBlock, lockStatusToSet));
        return lockStatusToSet;
    }

    private void isPermittedToRemoveBranch(String userId, UserRoleTO userRole, String branchId) {
        if (userRole == STAFF && !userService.findById(userId).getBranch().equals(branchId)) {
            throw MiddlewareModuleException.builder()
                          .devMsg("Insufficient permission to remove branch!")
                          .errorCode(INSUFFICIENT_PERMISSION)
                          .build();
        }
    }

    private void isExistingBranch(String branchId) {
        if (userService.countUsersByBranch(branchId) <= 0) {
            throw MiddlewareModuleException.builder()
                          .devMsg(String.format("Branch with id %s not found!", branchId))
                          .errorCode(BRANCH_NOT_FOUND)
                          .build();
        }
    }
}
