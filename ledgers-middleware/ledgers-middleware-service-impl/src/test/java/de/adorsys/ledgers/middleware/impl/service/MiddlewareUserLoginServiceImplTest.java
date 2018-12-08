package de.adorsys.ledgers.middleware.impl.service;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.exception.InsufficientPermissionMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.UserNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.impl.converter.UserMapper;
import de.adorsys.ledgers.um.api.domain.UserRoleBO;
import de.adorsys.ledgers.um.api.exception.InsufficientPermissionException;
import de.adorsys.ledgers.um.api.exception.UserNotFoundException;
import de.adorsys.ledgers.um.api.service.UserService;

@RunWith(MockitoJUnitRunner.class)
public class MiddlewareUserLoginServiceImplTest {
	private static final String ANY_TOKEN = "anyToken";
	private static final String LOGIN = "login";
	private static final String PIN = "pin";

	@InjectMocks
	private MiddlewareOnlineBankingServiceImpl middlewareUserService;

	@Mock
	private UserService userService;

	@Mock
	private UserMapper userMapper;

	@Test
	public void authorise() throws UserNotFoundException, UserNotFoundMiddlewareException, InsufficientPermissionException, InsufficientPermissionMiddlewareException {

		when(userService.authorise(LOGIN, PIN, UserRoleBO.CUSTOMER)).thenReturn(ANY_TOKEN);

		String isAuthorised = middlewareUserService.authorise(LOGIN, PIN, UserRoleTO.CUSTOMER);

		assertThat(isAuthorised, is(ANY_TOKEN));

		verify(userService, times(1)).authorise(LOGIN, PIN, UserRoleBO.CUSTOMER);
	}

	@Test(expected = UserNotFoundMiddlewareException.class)
	public void authoriseUserNotFound() throws UserNotFoundException, UserNotFoundMiddlewareException, InsufficientPermissionException, InsufficientPermissionMiddlewareException {

		when(userService.authorise(LOGIN, PIN, UserRoleBO.CUSTOMER)).thenThrow(UserNotFoundException.class);

		middlewareUserService.authorise(LOGIN, PIN, UserRoleTO.CUSTOMER);
	}
}