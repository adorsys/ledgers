package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.middleware.api.domain.um.ScaMethodTypeTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.util.exception.UserManagementModuleException;
import de.adorsys.ledgers.um.api.service.UserService;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import pro.javatar.commons.reader.YamlReader;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MiddlewareUserManagementServiceImplTest {

	@InjectMocks
	private MiddlewareUserManagementServiceImpl middlewareUserService;

	@Mock
	private UserService userService;

	private static UserBO userBO = null;
	private static UserTO userTO = null;

	@BeforeClass
	public static void before() {
		userBO = readYml(UserBO.class, "user.yml");
		userTO = readYml(UserTO.class, "user.yml");
	}

	@Test
	public void getSCAMethods() {
		String userLogin = "spe@adorsys.com.ua";
		when(userService.findByLogin(userLogin)).thenReturn(userBO);

		UserTO user = middlewareUserService.findByUserLogin(userLogin);

		assertThat(user.getScaUserData().size(), is(2));

		assertThat(user.getScaUserData().get(0).getScaMethod(), is(ScaMethodTypeTO.EMAIL));
		assertThat(user.getScaUserData().get(0).getMethodValue(), is("spe@adorsys.com.ua"));

		assertThat(user.getScaUserData().get(1).getScaMethod(), is(ScaMethodTypeTO.MOBILE));
		assertThat(user.getScaUserData().get(1).getMethodValue(), is("+380933686868"));

		verify(userService, times(1)).findByLogin(userLogin);
	}

	@Test(expected = UserManagementModuleException.class)
	public void findByUserLoginUserNotFound() {
		String login = "spe@adorsys.com.ua";

		when(userService.findByLogin(login)).thenThrow(UserManagementModuleException.builder().build());

		middlewareUserService.findByUserLogin(login);
	}

	@Test
	public void updateScaMethods() {
		String userLogin = "userLogin";
		when(userService.updateScaData(userBO.getScaUserData(), userLogin)).thenReturn(userBO);

		middlewareUserService.updateScaData(userLogin, userTO.getScaUserData());

		verify(userService, times(1)).updateScaData(userBO.getScaUserData(), userLogin);
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
