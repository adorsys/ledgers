package de.adorsys.ledgers.middleware.impl.service.upload;

import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareModuleException;
import de.adorsys.ledgers.middleware.api.service.MiddlewareUserManagementService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UploadUserServiceTest {
    @InjectMocks
    private UploadUserService service;

    @Mock
    private MiddlewareUserManagementService middlewareUserService;

    @Test
    void uploadUsers() {
        List<UserTO> result = service.uploadUsers(List.of(new UserTO()), "branch1");
        assertNotNull(result);
        verify(middlewareUserService, times(1)).create(any());
    }

    @Test
    void uploadUsers_user_present() {
        doThrow(MiddlewareModuleException.class).when(middlewareUserService).create(any());
        List<UserTO> result = service.uploadUsers(List.of(new UserTO()), "branch1");
        assertNotNull(result);
        verify(middlewareUserService, times(1)).create(any());
    }
}