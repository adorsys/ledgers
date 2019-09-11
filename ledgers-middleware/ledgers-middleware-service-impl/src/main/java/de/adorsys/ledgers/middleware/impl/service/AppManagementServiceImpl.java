package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.deposit.api.service.DepositAccountInitService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareModuleException;
import de.adorsys.ledgers.middleware.api.service.AppManagementService;
import de.adorsys.ledgers.um.api.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO.STAFF;
import static de.adorsys.ledgers.middleware.api.exception.MiddlewareErrorCode.BRANCH_NOT_FOUND;
import static de.adorsys.ledgers.middleware.api.exception.MiddlewareErrorCode.INSUFFICIENT_PERMISSION;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AppManagementServiceImpl implements AppManagementService {
    private static final int NANO_TO_SECOND = 1000000000;
    private final DepositAccountInitService depositAccountInitService;
    private final UserService userService;
    private final DepositAccountService depositAccountService;

    @Override
    public void initApp() {
        // Init deposit account config  data.
        log.info("Initiating Ledgers");
        long start = System.nanoTime();
        depositAccountInitService.initConfigData();
        log.info("Initiation completed in {} seconds", (double) (System.nanoTime() - start) / NANO_TO_SECOND);
    }

    @Override
    public void removeBranch(String userId, UserRoleTO userRole, String branchId) {
        log.info("User {} attempting delete branch {}", userId, branchId);
        long start = System.nanoTime();
        isExistingBranch(branchId);
        isPermittedToRemoveBranch(userId, userRole, branchId);
        log.info("Permission checked -> OK");
        depositAccountService.deleteBranch(branchId);
        log.info("Deleting branch {} Successful, in {} seconds", branchId, (double) (System.nanoTime() - start) / NANO_TO_SECOND);
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
