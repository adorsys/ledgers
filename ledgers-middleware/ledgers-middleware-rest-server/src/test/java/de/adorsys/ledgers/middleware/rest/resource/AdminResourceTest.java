package de.adorsys.ledgers.middleware.rest.resource;

import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.service.MiddlewareAccountManagementService;
import de.adorsys.ledgers.middleware.api.service.MiddlewareUserManagementService;
import de.adorsys.ledgers.util.domain.CustomPageImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

    @InjectMocks
    private AdminResource resource;

    @Test
    void users() {
        CustomPageImpl<UserTO> page = getUserPage();
        when(middlewareUserService.getUsersByBranchAndRoles(any(), any(), anyString(), any())).thenReturn(page);
        ResponseEntity<CustomPageImpl<UserTO>> result = resource.users(TPP_ID, ROLE, LOGIN, 1, 1);
        assertEquals(ResponseEntity.ok(page), result);
    }

    private CustomPageImpl<UserTO> getUserPage() {
        return new CustomPageImpl<>(1, 1, 1, 1, 1L, false, true, false, true, Collections.singletonList(getUser()));
    }

    @Test
    void accounts() {
        CustomPageImpl<AccountDetailsTO> account = getAccountPage();
        when(accountManagementService.getAccountsByOptionalBranchPaged(anyString(), anyString(), any())).thenReturn(account);
        ResponseEntity<CustomPageImpl<AccountDetailsTO>> result = resource.accounts(TPP_ID, IBAN, 0, 1);
        assertEquals(ResponseEntity.ok(account), result);
    }

    private CustomPageImpl<AccountDetailsTO> getAccountPage() {
        return new CustomPageImpl<>(1, 1, 1, 1, 1L, false, true, false, true, Collections.singletonList(new AccountDetailsTO()));
    }

    @Test
    void updatePassword() {
        ResponseEntity<Void> result = resource.updatePassword(TPP_ID, PIN);
        assertEquals(ResponseEntity.accepted().build(),result);
    }

    private UserTO getUser() {
        return new UserTO(LOGIN, "", PIN);
    }
}