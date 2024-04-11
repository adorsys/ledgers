/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.deposit.api.service.DepositAccountCleanupService;
import de.adorsys.ledgers.keycloak.client.api.KeycloakDataService;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MiddlewareCleanupServiceImplTest {

    private static final String USER_ID = "user-id";
    private static final String BRANCH = "branch";
    private static final String ACCOUNT_ID = "account-id";

    @InjectMocks
    private MiddlewareCleanupServiceImpl service;

    @Mock
    private UserService userService;
    @Mock
    private DepositAccountCleanupService depositAccountCleanupService;
    @Mock
    private KeycloakDataService keycloakDataService;

    @Test
    void deleteTransactions() {
        service.deleteTransactions(USER_ID, UserRoleTO.CUSTOMER, ACCOUNT_ID);
        verify(depositAccountCleanupService, times(1)).deleteTransactions(ACCOUNT_ID);
    }

    @Test
    void deleteAccount() {
        service.deleteAccount(USER_ID, UserRoleTO.CUSTOMER, ACCOUNT_ID);
        verify(depositAccountCleanupService, times(1)).deleteAccount(ACCOUNT_ID);
    }

    @Test
    void deleteUser() {
        UserBO userBO = new UserBO();
        userBO.setLogin("user_login");
        when(userService.findById(USER_ID)).thenReturn(userBO);

        service.deleteUser(USER_ID, UserRoleTO.CUSTOMER, USER_ID);

        verify(depositAccountCleanupService, times(1)).deleteUser(USER_ID);
        verify(keycloakDataService, times(1)).deleteUser("user_login");
    }

    @Test
    void removeBranch() {
        when(userService.findUserLoginsByBranch(BRANCH)).thenReturn(List.of("user_login"));

        service.removeBranch(USER_ID, UserRoleTO.CUSTOMER, BRANCH);

        verify(keycloakDataService, times(1)).deleteUser("user_login");
        verify(depositAccountCleanupService, times(1)).deleteBranch(BRANCH);
    }
}