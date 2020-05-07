package de.adorsys.ledgers.middleware.rest.resource;

import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.service.AppManagementService;
import de.adorsys.ledgers.middleware.api.service.MiddlewareAccountManagementService;
import de.adorsys.ledgers.middleware.api.service.MiddlewareUserManagementService;
import de.adorsys.ledgers.middleware.rest.annotation.MiddlewareResetResource;
import de.adorsys.ledgers.util.domain.CustomPageImpl;
import de.adorsys.ledgers.util.domain.CustomPageableImpl;
import lombok.RequiredArgsConstructor;
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


@RestController
@MiddlewareResetResource
@RequiredArgsConstructor
@RequestMapping("/admin" + AdminResourceAPI.BASE_PATH)
public class AdminResource implements AdminResourceAPI {
    private final MiddlewareUserManagementService middlewareUserService;
    private final MiddlewareAccountManagementService accountManagementService;
    private final AppManagementService appManagementService;

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
}
