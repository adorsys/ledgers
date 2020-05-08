package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.middleware.api.exception.MiddlewareModuleException;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.domain.UserRoleBO;
import de.adorsys.ledgers.um.api.service.UserService;
import de.adorsys.ledgers.util.exception.UserManagementModuleException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppManagementServiceImplTest {
    private static final String TPP_ID = "tppId";

    @Mock
    private UserService userService;

    @InjectMocks
    private AppManagementServiceImpl service;

    @Test
    void changeBlockedStatus() {
        when(userService.findById(anyString())).thenReturn(getUser(UserRoleBO.STAFF));
        boolean result = service.changeBlockedStatus(TPP_ID, true);
        assertTrue(result);
    }

    @Test
    void changeBlockedStatus_wrong_role() {
        when(userService.findById(anyString())).thenReturn(getUser(UserRoleBO.SYSTEM));
        assertThrows(MiddlewareModuleException.class, () -> service.changeBlockedStatus(TPP_ID, true));
    }

    @Test
    void changeBlockedStatus_user_nf() {
        when(userService.findById(anyString())).thenThrow(UserManagementModuleException.class);
        assertThrows(UserManagementModuleException.class, () -> service.changeBlockedStatus(TPP_ID, true));
    }

    private UserBO getUser(UserRoleBO role) {
        UserBO user = new UserBO("login", "email", "pin");
        user.setUserRoles(Collections.singletonList(role));
        return user;
    }
}