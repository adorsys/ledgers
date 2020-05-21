package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.middleware.api.domain.general.BbanStructure;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareModuleException;
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
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

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
    void changeBlockedStatus_user_nf() {
        when(userService.findById(anyString())).thenThrow(UserManagementModuleException.class);
        assertThrows(UserManagementModuleException.class, () -> service.changeBlockedStatus(TPP_ID, true));
    }

    private UserBO getUser(UserRoleBO role) {
        UserBO user = new UserBO("login", "email", "pin");
        user.setUserRoles(Collections.singletonList(role));
        return user;
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