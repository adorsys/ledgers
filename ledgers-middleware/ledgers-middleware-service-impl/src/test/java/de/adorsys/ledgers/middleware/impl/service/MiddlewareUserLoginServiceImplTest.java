package de.adorsys.ledgers.middleware.impl.service;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import de.adorsys.ledgers.middleware.api.domain.sca.SCALoginResponseTO;
import de.adorsys.ledgers.middleware.api.domain.um.AccessTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.exception.InsufficientPermissionMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.UserNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.impl.converter.BearerTokenMapper;
import de.adorsys.ledgers.middleware.impl.converter.UserMapper;
import de.adorsys.ledgers.um.api.domain.AccessTokenBO;
import de.adorsys.ledgers.um.api.domain.BearerTokenBO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.domain.UserRoleBO;
import de.adorsys.ledgers.um.api.exception.InsufficientPermissionException;
import de.adorsys.ledgers.um.api.exception.UserNotFoundException;
import de.adorsys.ledgers.um.api.service.UserService;

@RunWith(MockitoJUnitRunner.class)
public class MiddlewareUserLoginServiceImplTest {
	private static final String ACCESS_TOKEN = "access_token";
	private static final BearerTokenBO ANY_TOKEN_BO = new BearerTokenBO(ACCESS_TOKEN, 60,null,new AccessTokenBO());
	private static final BearerTokenTO ANY_TOKEN_TO = new BearerTokenTO(ACCESS_TOKEN, 60,null,new AccessTokenTO());
	private static final String LOGIN = "login";
	private static final String PIN = "pin";
	private static final UserBO USER = newUserBO();
	private static final String USER_ID = "USER_ID";
	private static UserBO newUserBO() {
		UserBO userBO = new UserBO(LOGIN, "user@user.de", PIN);
		userBO.setId(USER_ID);
		return userBO;
	}

	@InjectMocks
	private MiddlewareOnlineBankingServiceImpl middlewareUserService;

	@Mock
	private UserService userService;

	@Mock
	private UserMapper userMapper;

	@Mock
	private BearerTokenMapper bearerTokenMapper;
	
	@Mock
	private SCAUtils scaUtils;

	@Test
	public void authorise() throws UserNotFoundException, UserNotFoundMiddlewareException, InsufficientPermissionException, InsufficientPermissionMiddlewareException {

		when(userService.findByLogin(LOGIN)).thenReturn(USER);
		when(userService.authorise(LOGIN, PIN, UserRoleBO.valueOf("CUSTOMER"))).thenReturn(ANY_TOKEN_BO);
		when(userService.scaToken(anyString(), anyString(), anyInt(), any())).thenReturn(ANY_TOKEN_BO);
		when(bearerTokenMapper.toBearerTokenTO(ANY_TOKEN_BO)).thenReturn(ANY_TOKEN_TO);
		when(scaUtils.hasSCA(USER)).thenReturn(false);
		SCALoginResponseTO loginResponseTO = middlewareUserService.authorise(LOGIN, PIN, UserRoleTO.CUSTOMER);

		assertThat(loginResponseTO.getBearerToken().getAccess_token(), is(ACCESS_TOKEN));

		verify(userService, times(1)).authorise(LOGIN, PIN, UserRoleBO.valueOf("CUSTOMER"));
	}

	@Test(expected = UserNotFoundMiddlewareException.class)
	public void authoriseUserNotFound() throws UserNotFoundException, UserNotFoundMiddlewareException, InsufficientPermissionException, InsufficientPermissionMiddlewareException {

		when(userService.authorise(LOGIN, PIN, UserRoleBO.CUSTOMER)).thenThrow(UserNotFoundException.class);

		middlewareUserService.authorise(LOGIN, PIN, UserRoleTO.CUSTOMER);
	}
}