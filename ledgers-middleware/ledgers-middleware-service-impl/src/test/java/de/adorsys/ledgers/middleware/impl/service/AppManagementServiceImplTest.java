/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.deposit.api.service.DepositAccountInitService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.middleware.api.domain.general.BbanStructure;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.um.UploadedDataTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.service.MiddlewareUserManagementService;
import de.adorsys.ledgers.middleware.impl.service.upload.UploadBalanceService;
import de.adorsys.ledgers.middleware.impl.service.upload.UploadDepositAccountService;
import de.adorsys.ledgers.middleware.impl.service.upload.UploadPaymentService;
import de.adorsys.ledgers.middleware.impl.service.upload.UploadUserService;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.domain.UserRoleBO;
import de.adorsys.ledgers.um.api.service.UserService;
import de.adorsys.ledgers.util.exception.UserManagementModuleException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppManagementServiceImplTest {
    private static final String TPP_ID = "tppId";
    private static final String USER_ID = "kjk345knkj45";
    private static final String BRANCH = "branch";

    @Mock
    private DepositAccountService depositAccountService;
    @Mock
    private DepositAccountInitService depositAccountInitService;
    @Mock
    private UserService userService;
    @Mock
    private UploadUserService uploadUserService;
    @Mock
    private UploadDepositAccountService uploadDepositAccountService;
    @Mock
    private UploadBalanceService uploadBalanceService;
    @Mock
    private UploadPaymentService uploadPaymentService;
    @Mock
    private MiddlewareUserManagementService middlewareUserManagementService;

    @InjectMocks
    private AppManagementServiceImpl service;

    @Test
    void changeBlockedStatus() throws InterruptedException {
        when(userService.findById(anyString())).thenReturn(getUser(UserRoleBO.STAFF));
        boolean result = service.changeBlockedStatus(TPP_ID, true);
        Thread.sleep(500);

        assertTrue(result);

        verify(depositAccountService, times(1)).changeAccountsBlockedStatus(TPP_ID, true, true);
    }

    @Test
    void changeBlockedStatus_userNotStaff() {
        when(userService.findById(TPP_ID)).thenReturn(getUser(UserRoleBO.CUSTOMER));
        when(middlewareUserManagementService.changeStatus(TPP_ID, true)).thenReturn(true);

        boolean result = service.changeBlockedStatus(TPP_ID, true);
        assertTrue(result);

        verifyNoInteractions(depositAccountService);
    }

    @Test
    void changeBlockedStatus_user_nf() {
        when(userService.findById(anyString())).thenThrow(UserManagementModuleException.class);
        assertThrows(UserManagementModuleException.class, () -> service.changeBlockedStatus(TPP_ID, true));
    }

    @Test
    void nextBban() {
        BbanStructure structure = new BbanStructure();
        structure.setEntryType(BbanStructure.EntryType.N);
        structure.setLength(8);
        structure.setCountryPrefix("DE");
        String result = service.generateNextBban(structure);
        String regex = "^([0-9]{8})";
        assertTrue(isValid(regex, result));
    }

    @Test
    void initApp() {
        service.initApp();
        verify(depositAccountInitService, times(1)).initConfigData();
    }

    @Test
    void uploadData() throws InterruptedException {
        UploadedDataTO data = new UploadedDataTO();
        data.setUsers(Collections.emptyList());
        ScaInfoTO info = new ScaInfoTO();
        info.setUserId(USER_ID);
        UserBO userBO = new UserBO();
        userBO.setBranch(BRANCH);
        List<UserTO> uploadedUsers = Collections.singletonList(new UserTO());

        when(userService.findById(USER_ID)).thenReturn(userBO);
        when(uploadUserService.uploadUsers(data.getUsers(), BRANCH)).thenReturn(uploadedUsers);

        service.uploadData(data, info);
        Thread.sleep(500);

        verify(uploadDepositAccountService, times(1)).uploadDepositAccounts(uploadedUsers, data.getDetails(), info);
        verify(uploadBalanceService, times(1)).uploadBalances(data, info);
        verify(uploadPaymentService, times(1)).uploadPayments(data, info);
    }

    private UserBO getUser(UserRoleBO role) {
        UserBO user = new UserBO("login", "email", "pin");
        user.setId(TPP_ID);
        user.setUserRoles(Collections.singletonList(role));
        return user;
    }

    @Test
    void nextBban_one_present() {
        when(userService.isPresentBranchCode(any()))
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);
        BbanStructure structure = new BbanStructure();
        structure.setEntryType(BbanStructure.EntryType.N);
        structure.setLength(8);
        structure.setCountryPrefix("DE");
        String result = service.generateNextBban(structure);
        String regex = "^([0-9]{8})";
        assertTrue(isValid(regex, result));
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(userService, times(3)).isPresentBranchCode(captor.capture());
        assertTrue(captor.getAllValues().stream().allMatch(s -> s.startsWith("DE_")));
    }

    public static boolean isValid(String regex, String str) {
        return Pattern.compile(regex).matcher(str).find();
    }
}