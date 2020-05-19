package de.adorsys.ledgers.middleware.rest.resource;

import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareErrorCode;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareModuleException;
import de.adorsys.ledgers.middleware.api.service.AppManagementService;
import de.adorsys.ledgers.middleware.api.service.MiddlewareAccountManagementService;
import de.adorsys.ledgers.middleware.api.service.MiddlewareUserManagementService;
import de.adorsys.ledgers.middleware.impl.converter.UserMapper;
import de.adorsys.ledgers.middleware.rest.annotation.MiddlewareResetResource;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.service.UserService;
import de.adorsys.ledgers.util.domain.CustomPageImpl;
import de.adorsys.ledgers.util.domain.CustomPageableImpl;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO.CUSTOMER;
import static de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO.STAFF;
import static de.adorsys.ledgers.middleware.rest.resource.UserMgmtStaffResourceAPI.USER_CANNOT_REGISTER_IN_BRANCH;


@RestController
@MiddlewareResetResource
@RequiredArgsConstructor
@RequestMapping(AdminResourceAPI.BASE_PATH)
public class AdminResource implements AdminResourceAPI {
    private final MiddlewareUserManagementService middlewareUserService;
    private final MiddlewareAccountManagementService accountManagementService;
    private final AppManagementService appManagementService;
    private final UserService userService;
    private final UserMapper userMapper;

    @Override
    @PreAuthorize("hasRole('SYSTEM')")
    public ResponseEntity<CustomPageImpl<UserTO>> users(String countryCode, String branchId, String branchLogin, String userLogin, UserRoleTO role, Boolean blocked, int page, int size) {
        CustomPageableImpl pageable = new CustomPageableImpl(page, size);
        List<UserRoleTO> roles = Optional.ofNullable(role).map(Collections::singletonList).orElseGet(() -> Arrays.asList(STAFF, CUSTOMER));
        return ResponseEntity.ok(middlewareUserService.getUsersByBranchAndRoles(countryCode, branchId, branchLogin, userLogin, roles, blocked, pageable));
    }

    @Override
    @PreAuthorize("hasRole('SYSTEM')")
    public ResponseEntity<CustomPageImpl<AccountDetailsTO>> accounts(String countryCode, String branchId, String branchLogin, String iban, Boolean blocked, int page, int size) {
        CustomPageableImpl pageable = new CustomPageableImpl(page, size);
        return ResponseEntity.ok(accountManagementService.getAccountsByBranchAndMultipleParams(countryCode, branchId, branchLogin, iban, blocked, pageable));
    }

    @Override
    @PreAuthorize("hasRole('SYSTEM')")
    public ResponseEntity<Void> updatePassword(String branchId, String password) {
        middlewareUserService.updatePassword(branchId, password);
        return ResponseEntity.accepted().build();
    }

    @Override
    @PreAuthorize("hasRole('SYSTEM')")
    public ResponseEntity<Boolean> changeStatus(String branchId) {
        return ResponseEntity.ok(appManagementService.changeBlockedStatus(branchId, false));
    }

    @Override
    @PreAuthorize("hasRole('SYSTEM')")
    public ResponseEntity<UserTO> register(UserTO user) {
        if (user.getUserRoles().contains(STAFF) && middlewareUserService.countUsersByBranch(user.getBranch()) > 0) {
            throw MiddlewareModuleException.builder()
                          .errorCode(MiddlewareErrorCode.INSUFFICIENT_PERMISSION)
                          .devMsg(USER_CANNOT_REGISTER_IN_BRANCH)
                          .build();
        }
        UserTO createdUser = middlewareUserService.create(user);
        createdUser.setPin(null);
        return ResponseEntity.ok(createdUser);
    }

    @Override
    @PreAuthorize("hasRole('SYSTEM')")
    public ResponseEntity<Void> user(UserTO user) {
        checkUpdateData(user);
        middlewareUserService.updateUser(user.getBranch(), user);
        return ResponseEntity.accepted().build();
    }

    @SuppressWarnings("PMD.CyclomaticComplexity")
    private void checkUpdateData(UserTO user) {
        UserBO userStored = userService.findById(user.getId());
        if (userStored.isBlocked() || userStored.isSystemBlocked()) {
            throw MiddlewareModuleException.builder()
                          .errorCode(MiddlewareErrorCode.USER_IS_BLOCKED)
                          .devMsg("You are not allowed to modify a blocked user!")
                          .build();
        }
        if (!userStored.getUserRoles().containsAll(userMapper.toUserBO(user).getUserRoles())) {
            throw MiddlewareModuleException.builder()
                          .errorCode(MiddlewareErrorCode.INSUFFICIENT_PERMISSION)
                          .devMsg("You are not allowed to modify users roles!")
                          .build();
        }
        if (!StringUtils.equals(userStored.getBranch(), user.getBranch())
                    || !StringUtils.equals(user.getBranch(), user.getId())
                    || !StringUtils.equals(user.getId(), userStored.getId())) {
            throw MiddlewareModuleException.builder()
                          .errorCode(MiddlewareErrorCode.INSUFFICIENT_PERMISSION)
                          .devMsg("User are not allowed to modify users TPP relation!")
                          .build();
        }
    }
}
