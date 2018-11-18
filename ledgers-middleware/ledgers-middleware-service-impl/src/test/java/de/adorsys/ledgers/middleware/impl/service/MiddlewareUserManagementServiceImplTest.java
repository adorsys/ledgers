package de.adorsys.ledgers.middleware.impl.service;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import de.adorsys.ledgers.middleware.api.domain.um.ScaMethodTypeTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.exception.UserNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.impl.converter.UserMapper;
import de.adorsys.ledgers.middleware.impl.service.MiddlewareUserManagementServiceImpl;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.exception.UserNotFoundException;
import de.adorsys.ledgers.um.api.service.UserService;
import pro.javatar.commons.reader.YamlReader;

@RunWith(MockitoJUnitRunner.class)
public class MiddlewareUserManagementServiceImplTest {
	private static final String LOGIN = "login";
	private static final String PIN = "pin";

	@InjectMocks
	private MiddlewareUserManagementServiceImpl middlewareUserService;

	@Mock
	private UserService userService;

	@Mock
	private UserMapper userMapper;

	private static UserBO userBO = null;
	private static UserTO userTO = null;

	@BeforeClass
	public static void before() {
		userBO = readYml(UserBO.class, "user.yml");
		userTO = readYml(UserTO.class, "user.yml");
	}

	@Test
	public void authorise() throws UserNotFoundException, UserNotFoundMiddlewareException {

		when(userService.authorise(LOGIN, PIN)).thenReturn(Boolean.TRUE);

		boolean isAuthorised = middlewareUserService.authorise(LOGIN, PIN);

		assertThat(isAuthorised, is(Boolean.TRUE));

		verify(userService, times(1)).authorise(LOGIN, PIN);
	}

	@Test(expected = UserNotFoundMiddlewareException.class)
	public void authoriseUserNotFound() throws UserNotFoundException, UserNotFoundMiddlewareException {

		when(userService.authorise(LOGIN, PIN)).thenThrow(UserNotFoundException.class);

		middlewareUserService.authorise(LOGIN, PIN);
	}

	@Test
	public void getSCAMethods() throws UserNotFoundException, UserNotFoundMiddlewareException {
		String userLogin = "spe@adorsys.com.ua";
		when(userService.findByLogin(userLogin)).thenReturn(userBO);
		when(userMapper.toUserTO(userBO)).thenReturn(userTO);

		UserTO user = middlewareUserService.findByUserLogin(userLogin);

		assertThat(user.getScaUserData().size(), is(2));

		assertThat(user.getScaUserData().get(0).getScaMethod(), is(ScaMethodTypeTO.EMAIL));
		assertThat(user.getScaUserData().get(0).getMethodValue(), is("spe@adorsys.com.ua"));

		assertThat(user.getScaUserData().get(1).getScaMethod(), is(ScaMethodTypeTO.MOBILE));
		assertThat(user.getScaUserData().get(1).getMethodValue(), is("+380933686868"));

		verify(userService, times(1)).findByLogin(userLogin);
		verify(userMapper, times(1)).toUserTO(userBO);
	}

	@Test(expected = UserNotFoundMiddlewareException.class)
	public void findByUserLoginUserNotFound() throws UserNotFoundException, UserNotFoundMiddlewareException {
		String login = "spe@adorsys.com.ua";

		when(userService.findByLogin(login)).thenThrow(new UserNotFoundException());

		middlewareUserService.findByUserLogin(login);
	}

	@Test
	public void updateScaMethods() throws UserNotFoundException, UserNotFoundMiddlewareException {
		String userLogin = "userLogin";
		when(userService.updateScaData(userBO.getScaUserData(), userLogin)).thenReturn(userBO);
		when(userMapper.toScaUserDataListBO(userTO.getScaUserData())).thenReturn(userBO.getScaUserData());

		middlewareUserService.updateScaData(userLogin, userTO.getScaUserData());

		verify(userMapper, times(1)).toScaUserDataListBO(userTO.getScaUserData());
		verify(userService, times(1)).updateScaData(userBO.getScaUserData(), userLogin);
		verify(userMapper, times(1)).toUserTO(userBO);
	}

    private static <T> T readYml(Class<T> aClass, String fileName) {
        try {
            return YamlReader.getInstance().getObjectFromResource(MiddlewareUserManagementServiceImplTest.class, fileName, aClass);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}