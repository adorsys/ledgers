/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.rest.resource;

import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsExtendedTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserExtendedTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareModuleException;
import de.adorsys.ledgers.middleware.api.service.AppManagementService;
import de.adorsys.ledgers.middleware.api.service.MiddlewareAccountManagementService;
import de.adorsys.ledgers.middleware.api.service.MiddlewareUserManagementService;
import de.adorsys.ledgers.middleware.impl.converter.UserMapper;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.domain.UserRoleBO;
import de.adorsys.ledgers.um.api.service.UserService;
import de.adorsys.ledgers.util.domain.CustomPageImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminResourceTest {
    private static final String TPP_ID = "tpp_id";
    private static final String IBAN = "iban";
    private static final String LOGIN = "login";
    private static final String PIN = "pin";
    private static final UserRoleTO ROLE = UserRoleTO.CUSTOMER;
    @Mock
    private MiddlewareUserManagementService middlewareUserService;
    @Mock
    private MiddlewareAccountManagementService accountManagementService;
    @Mock
    private AppManagementService appManagementService;
    @Mock
    private UserService userService;

    @InjectMocks
    private AdminResource resource;

    @Test
    void users() {
        CustomPageImpl<UserExtendedTO> page = getUserPage();
        when(middlewareUserService.getUsersByBranchAndRolesExtended(anyString(), anyString(), anyString(), anyString(), anyList(), anyBoolean(), any())).thenReturn(page);
        ResponseEntity<CustomPageImpl<UserExtendedTO>> result = resource.users("DE", TPP_ID, "", LOGIN, ROLE, false, 1, 1);
        assertEquals(ResponseEntity.ok(page), result);
    }

    private CustomPageImpl<UserExtendedTO> getUserPage() {
        return new CustomPageImpl<>(1, 1, 1, 1, 1L, false, true, false, true, Collections.singletonList(getUserExtended()));
    }

    @Test
    void accounts() {
        CustomPageImpl<AccountDetailsExtendedTO> account = getAccountPage();
        when(accountManagementService.getAccountsByBranchAndMultipleParams(anyString(), anyString(), anyString(), anyString(), anyBoolean(), any())).thenReturn(account);
        ResponseEntity<CustomPageImpl<AccountDetailsExtendedTO>> result = resource.accounts("", TPP_ID, "", IBAN, false, 0, 1);
        assertEquals(ResponseEntity.ok(account), result);
    }

    private CustomPageImpl<AccountDetailsExtendedTO> getAccountPage() {
        return new CustomPageImpl<>(1, 1, 1, 1, 1L, false, true, false, true, Collections.singletonList(new AccountDetailsExtendedTO()));
    }

    @Test
    void updatePassword() {
        ResponseEntity<Void> result = resource.updatePassword(TPP_ID, PIN);
        assertEquals(ResponseEntity.accepted().build(), result);
    }

    private UserExtendedTO getUserExtended() {
        UserExtendedTO to = new UserExtendedTO();
        to.setLogin(LOGIN);
        to.setPin(PIN);
        return to;
    }

    @Test
    void changeStatus() {
        when(appManagementService.changeBlockedStatus(anyString(), anyBoolean())).thenReturn(true);
        ResponseEntity<Boolean> result = resource.changeStatus(TPP_ID);
        assertEquals(ResponseEntity.ok(true), result);
    }

    @Test
    void user() {
        when(userService.findById(any())).thenReturn(getUserBO(TPP_ID, TPP_ID, UserRoleBO.STAFF));
        ReflectionTestUtils.setField(resource, "userMapper", Mappers.getMapper(UserMapper.class));

        ResponseEntity<Void> response = resource.user(getUserTO(TPP_ID, TPP_ID, UserRoleTO.STAFF));
        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
    }

    @Test
    void register() {
        when(middlewareUserService.create(any())).thenReturn(getUserTO(TPP_ID, TPP_ID, UserRoleTO.STAFF));
        ResponseEntity<UserTO> result = resource.register(getUserTO(TPP_ID, TPP_ID, UserRoleTO.STAFF));
        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void user_diff_in_roles() {
        when(userService.findById(any())).thenReturn(getUserBO(TPP_ID, TPP_ID, UserRoleBO.STAFF));
        ReflectionTestUtils.setField(resource, "userMapper", Mappers.getMapper(UserMapper.class));
        UserTO userTO = getUserTO(TPP_ID, TPP_ID, UserRoleTO.SYSTEM);
        assertThrows(MiddlewareModuleException.class, () -> resource.user(userTO));
    }

    @Test
    void user_diff_in_branch() {
        when(userService.findById(any())).thenReturn(getUserBO(TPP_ID, TPP_ID + 1, UserRoleBO.STAFF));
        ReflectionTestUtils.setField(resource, "userMapper", Mappers.getMapper(UserMapper.class));
        UserTO userTO = getUserTO(TPP_ID, TPP_ID, UserRoleTO.STAFF);
        assertThrows(MiddlewareModuleException.class, () -> resource.user(userTO));
    }

    @Test
    void user_blocked() {
        UserBO userFromDb = getUserBO(TPP_ID, TPP_ID + 1, UserRoleBO.STAFF);
        userFromDb.setBlocked(true);
        when(userService.findById(any())).thenReturn(userFromDb);
        ReflectionTestUtils.setField(resource, "userMapper", Mappers.getMapper(UserMapper.class));
        UserTO userTO = getUserTO(TPP_ID, TPP_ID, UserRoleTO.STAFF);
        assertThrows(MiddlewareModuleException.class, () -> resource.user(userTO));
    }

    private UserBO getUserBO(String id, String branch, UserRoleBO role) {
        UserBO bo = new UserBO(LOGIN, "", PIN);
        bo.setId(id);
        bo.setBranch(branch);
        bo.getUserRoles().add(role);
        return bo;
    }

    private UserTO getUserTO(String id, String branch, UserRoleTO role) {
        UserTO bo = new UserTO(LOGIN, "", PIN);
        bo.setId(id);
        bo.setBranch(branch);
        bo.getUserRoles().add(role);
        return bo;
    }
}