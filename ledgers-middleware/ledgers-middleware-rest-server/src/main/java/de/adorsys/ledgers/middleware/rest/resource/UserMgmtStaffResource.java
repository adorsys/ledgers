/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.rest.resource;

import de.adorsys.ledgers.middleware.api.domain.general.RevertRequestTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.um.AccountAccessTO;
import de.adorsys.ledgers.middleware.api.domain.um.ScaUserDataTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.service.MiddlewareRecoveryService;
import de.adorsys.ledgers.middleware.api.service.MiddlewareUserManagementService;
import de.adorsys.ledgers.middleware.rest.annotation.MiddlewareUserResource;
import de.adorsys.ledgers.middleware.rest.security.ScaInfoHolder;
import de.adorsys.ledgers.util.domain.CustomPageImpl;
import de.adorsys.ledgers.util.domain.CustomPageableImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RestController
@MiddlewareUserResource
@RequiredArgsConstructor
@RequestMapping("/staff-access" + UserMgmtRestAPI.BASE_PATH)
public class UserMgmtStaffResource implements UserMgmtStaffResourceAPI {
    private final MiddlewareUserManagementService middlewareUserService;
    private final ScaInfoHolder scaInfoHolder;
    private final MiddlewareRecoveryService middlewareRecoveryService;

    @Override
    @PreAuthorize("@accountAccessSecurityFilter.isNewStaffUser(#branchStaff)")
    public ResponseEntity<UserTO> register(String branch, UserTO branchStaff) {
        branchStaff.setBranch(branch);
        branchStaff.setUserRoles(Collections.singletonList(UserRoleTO.STAFF));
        UserTO user = middlewareUserService.create(branchStaff);
        user.setPin(null);

        return ResponseEntity.ok(user);
    }

    @Override
    @PreAuthorize("@accountAccessSecurityFilter.hasManagerAccessToUser(#user.id)")
    public ResponseEntity<UserTO> modifyUser(String branch, UserTO user) {
        return ResponseEntity.ok(middlewareUserService.updateUser(branch, user));
    }

    @Override
    @PreAuthorize("@accountAccessSecurityFilter.hasRole('STAFF')")
    public ResponseEntity<UserTO> createUser(UserTO user) {
        UserTO branchStaff = middlewareUserService.findById(scaInfoHolder.getScaInfo().getUserId());

        // set the same branch for the user the staff member that creates it
        user.setBranch(branchStaff.getBranch());

        // Assert that the role is neither system nor technical
        user.getUserRoles().removeAll(Arrays.asList(UserRoleTO.SYSTEM, UserRoleTO.TECHNICAL));

        UserTO newUser = middlewareUserService.create(user);
        newUser.setPin(null);

        return ResponseEntity.ok(newUser);
    }

    @Override
    @PreAuthorize("@accountAccessSecurityFilter.hasRole('STAFF')")
    public ResponseEntity<CustomPageImpl<UserTO>> getBranchUsersByRoles(List<UserRoleTO> roles, String queryParam, Boolean blockedParam, int page, int size) {
        CustomPageableImpl pageable = new CustomPageableImpl(page, size);
        UserTO branchStaff = middlewareUserService.findById(scaInfoHolder.getUserId());
        CustomPageImpl<UserTO> users = middlewareUserService.getUsersByBranchAndRoles("", branchStaff.getBranch(), "", queryParam, roles, blockedParam, pageable);
        return ResponseEntity.ok(users);
    }

    @Override
    @PreAuthorize("@accountAccessSecurityFilter.hasRole('STAFF')")
    public ResponseEntity<List<String>> getBranchUserLogins() {
        UserTO branchStaff = middlewareUserService.findById(scaInfoHolder.getUserId());
        List<String> users = middlewareUserService.getBranchUserLogins(branchStaff.getBranch());
        return ResponseEntity.ok(users);
    }

    @Override
    @PreAuthorize("@accountAccessSecurityFilter.hasRole('SYSTEM')")
    public ResponseEntity<List<String>> getBranchUserLoginsByBranchId(String branchId) {
        UserTO branchStaff = middlewareUserService.findById(branchId);
        List<String> users = middlewareUserService.getBranchUserLogins(branchStaff.getBranch());
        return ResponseEntity.ok(users);
    }

    @Override
    @PreAuthorize("@accountAccessSecurityFilter.hasManagerAccessToUser(#userId)")
    public ResponseEntity<UserTO> getBranchUserById(String userId) {
        UserTO user = middlewareUserService.findById(userId);
        return ResponseEntity.ok(user);
    }

    @Override
    @PreAuthorize("@accountAccessSecurityFilter.hasManagerAccessToUser(#userId)")
    public ResponseEntity<Void> updateUserScaData(String userId, List<ScaUserDataTO> data) {
        UserTO userWithUpdatedSca = middlewareUserService.updateScaData(middlewareUserService.findById(userId).getLogin(), data);
        URI uri = UriComponentsBuilder.fromUriString("/staff-access" + UserMgmtRestAPI.BASE_PATH + "/" + userWithUpdatedSca.getId())
                          .build().toUri();
        return ResponseEntity.created(uri).build();
    }

    @Override
    @PreAuthorize("@accountAccessSecurityFilter.hasAnyRole('STAFF','SYSTEM')")
    //TODO Check Account enabled, check initiator has accessTo Account, Check Same Branch as User/Check user is not a branch!!!, AccountExists
    public ResponseEntity<Void> updateAccountAccessForUser(String userId, AccountAccessTO access) {
        ScaInfoTO scaInfo = scaInfoHolder.getScaInfo();
        middlewareUserService.updateAccountAccess(scaInfo, userId, access);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    @PreAuthorize("@accountAccessSecurityFilter.hasManagerAccessToUser(#userId)")
    public ResponseEntity<Boolean> changeStatus(String userId) {
        return ResponseEntity.ok(middlewareUserService.changeStatus(userId, false));
    }

    @Override
    @PreAuthorize("@accountAccessSecurityFilter.hasAnyRole('STAFF') and @accountAccessSecurityFilter.isSameUser(#request.branchId)")
    public ResponseEntity<Void> revertDatabase(RevertRequestTO request) {
        middlewareRecoveryService.revertDatabase(request.getBranchId(), request.getRecoveryPointId());
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
