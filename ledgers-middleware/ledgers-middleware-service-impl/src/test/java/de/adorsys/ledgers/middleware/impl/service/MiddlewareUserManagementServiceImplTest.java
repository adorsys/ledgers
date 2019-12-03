package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.middleware.api.domain.account.AccountReferenceTO;
import de.adorsys.ledgers.middleware.api.domain.um.ScaMethodTypeTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareModuleException;
import de.adorsys.ledgers.um.api.domain.AccessTypeBO;
import de.adorsys.ledgers.um.api.domain.AccountAccessBO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.service.UserService;
import de.adorsys.ledgers.util.exception.UserManagementModuleException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.junit.MockitoJUnitRunner;
import pro.javatar.commons.reader.YamlReader;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MiddlewareUserManagementServiceImplTest {
    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Currency USD = Currency.getInstance("USD");

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

        assertThat(user.getScaUserData().size()).isEqualTo((2));

        assertThat(user.getScaUserData().get(0).getScaMethod()).isEqualByComparingTo(ScaMethodTypeTO.EMAIL);
        assertThat(user.getScaUserData().get(0).getMethodValue()).isEqualTo("spe@adorsys.com.ua");

        assertThat(user.getScaUserData().get(1).getScaMethod()).isEqualTo(ScaMethodTypeTO.MOBILE);
        assertThat(user.getScaUserData().get(1).getMethodValue()).isEqualTo("+380933686868");

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

    @Test
    public void checkMultilevelScaRequired_no_multilevel() {
        Whitebox.setInternalState(middlewareUserService, "multilevelScaEnable", true);
        when(userService.findByLogin(any())).thenReturn(getUser());
        boolean response = middlewareUserService.checkMultilevelScaRequired("some_login", getReferences("1"));
        assertThat(response).isFalse();
    }

    @Test
    public void checkMultilevelScaRequired_empty_list() {
        Whitebox.setInternalState(middlewareUserService, "multilevelScaEnable", true);
        when(userService.findByLogin(any())).thenReturn(getUser());
        boolean response = middlewareUserService.checkMultilevelScaRequired("some_login", new ArrayList<>());
        assertThat(response).isTrue();
    }

    @Test
    public void checkMultilevelScaRequired_2_acc_with_mlsca() {
        Whitebox.setInternalState(middlewareUserService, "multilevelScaEnable", true);
        when(userService.findByLogin(any())).thenReturn(getUser());
        boolean response = middlewareUserService.checkMultilevelScaRequired("some_login", getReferences("1", "2"));
        assertThat(response).isTrue();
    }

    @Test
    public void checkMultilevelScaRequired_1_acc_no_curr_with_mlsca() {
        Whitebox.setInternalState(middlewareUserService, "multilevelScaEnable", true);
        when(userService.findByLogin(any())).thenReturn(getUser());
        boolean response = middlewareUserService.checkMultilevelScaRequired("some_login", Collections.singletonList(getReference("1", null)));
        assertThat(response).isTrue();
    }

    @Test(expected = MiddlewareModuleException.class)
    public void checkMultilevelScaRequired_acc_not_match() {
        Whitebox.setInternalState(middlewareUserService, "multilevelScaEnable", true);
        when(userService.findByLogin(any())).thenReturn(getUser());
        middlewareUserService.checkMultilevelScaRequired("some_login", getReferences("1", "3"));
    }

    @Test
    public void checkMultilevelScaRequired_multilevel_false() {
        Whitebox.setInternalState(middlewareUserService, "multilevelScaEnable", false);
        boolean response = middlewareUserService.checkMultilevelScaRequired("some_login", getReferences("1", "2"));
        assertThat(response).isFalse();
    }

    private List<AccountReferenceTO> getReferences(String... ibans) {
        return Arrays.stream(ibans)
                       .map(i -> getReference(i, EUR))
                       .collect(Collectors.toList());
    }

    private AccountReferenceTO getReference(String iban, Currency currency) {
        return new AccountReferenceTO(iban, null, null, null, null, currency);
    }

    private UserBO getUser() {
        UserBO user = new UserBO("", "", "");
        user.setAccountAccesses(getAccesses());
        return user;
    }

    private List<AccountAccessBO> getAccesses() {
        return Arrays.asList(
                getAccess("1", EUR, 100),
                getAccess("1", USD, 50),
                getAccess("2", EUR, 60)
        );
    }

    private AccountAccessBO getAccess(String iban, Currency currency, int scaWeight) {
        AccountAccessBO access = new AccountAccessBO(iban, AccessTypeBO.OWNER);
        access.setCurrency(currency);
        access.setScaWeight(scaWeight);
        return access;
    }
}
