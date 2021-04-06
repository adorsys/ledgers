package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.deposit.api.service.DepositAccountInitService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.middleware.api.domain.general.BbanStructure;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.um.UploadedDataTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.service.AppManagementService;
import de.adorsys.ledgers.middleware.api.service.MiddlewareUserManagementService;
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
    private final MiddlewareUserManagementService middlewareUserManagementService;

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
    public void uploadData(UploadedDataTO data, ScaInfoTO info) {
        String branchId = userService.findById(info.getUserId()).getBranch();
        List<UserTO> uploadedUsers = uploadUserService.uploadUsers(data.getUsers(), branchId);
        uploadDepositAccountService.uploadDepositAccounts(uploadedUsers, data.getDetails(), info);
        CompletableFuture.runAsync(() -> uploadBalanceService.uploadBalances(data, info), FIXED_THREAD_POOL)
                .thenRunAsync(() -> uploadPaymentService.uploadPayments(data, info));
    }

    @Override
    public boolean changeBlockedStatus(String userId, boolean isSystemBlock) {
        UserBO user = userService.findById(userId);
        boolean lockStatusToSet = isSystemBlock ? !user.isSystemBlocked() : !user.isBlocked();

        // TPP cases.
        if (user.getUserRoles().contains(UserRoleBO.STAFF)) { //TODO See MiddlewareUserManagementServiceImpl::234 for DUPE!
            CompletableFuture.runAsync(() -> userService.setBranchBlockedStatus(userId, isSystemBlock, lockStatusToSet), FIXED_THREAD_POOL)
                    .thenRunAsync(() -> depositAccountService.changeAccountsBlockedStatus(userId, isSystemBlock, lockStatusToSet));
            return lockStatusToSet;
        }

        // Cases for customers and admins
        return middlewareUserManagementService.changeStatus(userId, isSystemBlock);
    }

    @Override
    public String generateNextBban(BbanStructure structure) {
        String bban = structure.generateRandomBban();
        while (userService.isPresentBranchCode(structure.getCountryPrefix() + "_" + bban)) {
            bban = structure.generateRandomBban();
        }
        return bban;
    }
}
