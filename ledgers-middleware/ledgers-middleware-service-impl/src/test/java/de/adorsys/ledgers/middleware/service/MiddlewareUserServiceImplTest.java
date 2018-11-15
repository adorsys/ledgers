package de.adorsys.ledgers.middleware.service;

import de.adorsys.ledgers.middleware.service.exception.UserNotFoundMiddlewareException;
import de.adorsys.ledgers.um.api.exception.UserNotFoundException;
import de.adorsys.ledgers.um.api.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MiddlewareUserServiceImplTest {
    private static final String LOGIN = "login";
    private static final String PIN = "pin";

    @InjectMocks
    private MiddlewareUserServiceImpl middlewareUserService;

    @Mock
    private UserService service;

    @Test
    public void authorise() throws UserNotFoundException, UserNotFoundMiddlewareException {

        when(service.authorise(LOGIN, PIN)).thenReturn(Boolean.TRUE);

        boolean isAuthorised = middlewareUserService.authorise(LOGIN, PIN);

        assertThat(isAuthorised, is(Boolean.TRUE));

        verify(service, times(1)).authorise(LOGIN, PIN);
    }

    @Test(expected = UserNotFoundMiddlewareException.class)
    public void authoriseUserNotFound() throws UserNotFoundException, UserNotFoundMiddlewareException {

        when(service.authorise(LOGIN, PIN)).thenThrow(UserNotFoundException.class);

        middlewareUserService.authorise(LOGIN, PIN);
    }
}