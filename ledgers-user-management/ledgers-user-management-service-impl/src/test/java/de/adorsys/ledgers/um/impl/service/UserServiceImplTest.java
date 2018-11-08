package de.adorsys.ledgers.um.impl.service;

import de.adorsys.ledgers.um.api.domain.AccessTypeBO;
import de.adorsys.ledgers.um.api.domain.AccountAccessBO;
import de.adorsys.ledgers.um.api.domain.ScaUserDataBO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.exception.UserNotFoundException;
import de.adorsys.ledgers.um.db.domain.AccountAccess;
import de.adorsys.ledgers.um.db.domain.AccessType;
import de.adorsys.ledgers.um.db.domain.UserEntity;
import de.adorsys.ledgers.um.db.repository.UserRepository;
import de.adorsys.ledgers.um.impl.converter.UserConverter;
import de.adorsys.ledgers.util.MD5Util;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import pro.javatar.commons.reader.ResourceReader;
import pro.javatar.commons.reader.YamlReader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceImplTest {

    @InjectMocks
    public UserServiceImpl userService;

    @Mock
    private UserRepository repository;

    @Mock
    private UserConverter converter;

    private ResourceReader reader = YamlReader.getInstance();

    private static final String USER_ID = "SomeUniqueID";
    private static final String USER_EMAIL = "vne@adorsys.de";
    private static final String USER_LOGIN = "vne";
    private static final String USER_PIN = "12345678";
    private static final String USER_NON_EXISTING_LOGIN = "NonExistingLogin";
    private static final String USER_NON_EXISTING_ID = "NonExistingID";
    private UserEntity userEntity;
    private UserBO userBO;
    private static final String USER_IBAN = "3737463673647";
    private static final AccessTypeBO USER_ACC_ACCESS_TYPE_1 = AccessTypeBO.READ;
    private static final AccessTypeBO USER_ACC_ACCESS_TYPE_2 = AccessTypeBO.DISPOSE;


    @Before
    public void setUp() {
        userEntity = readUserEntity(reader);
        userBO = readUserBO(reader);
    }

    @Test
    public void authorizeWithLoginAndPin() throws UserNotFoundException {
        UserEntity userEntity = readUserEntity(reader);

        when(repository.findFirstByLogin(USER_LOGIN)).thenReturn(Optional.of(userEntity));

        boolean auth = userService.authorize(USER_LOGIN, USER_PIN);

        assertThat(auth, is(true));
    }

    @Test
    public void findById() throws UserNotFoundException {

        when(repository.findById(USER_ID)).thenReturn(Optional.of(userEntity));
        when(converter.toUserBO(userEntity)).thenReturn(userBO);

        UserBO user = userService.findById(USER_ID);

        assertThat(user.getId(), is(USER_ID));
        assertThat(user.getEmail(), is(USER_EMAIL));
        assertThat(user.getLogin(), is(USER_LOGIN));
        assertThat(MD5Util.encode(user.getPin()), is(MD5Util.encode(USER_PIN)));

        verify(repository, times(1)).findById(USER_ID);
        verify(converter, times(1)).toUserBO(userEntity);
    }

    @Test
    public void getUserScaData() throws UserNotFoundException {
        UserEntity userEntity = readUserEntity(reader);
        UserBO userBO = readUserBO(reader);

        when(repository.findById(USER_ID)).thenReturn(Optional.of(userEntity));
        when(converter.toUserBO(userEntity)).thenReturn(userBO);

        List<ScaUserDataBO> userData = userService.getUserScaData(USER_ID);

        assertThat(userData.size(), is(2));
        assertThat(userData.get(0).getMethodValue(), is(USER_EMAIL));

        verify(repository, times(1)).findById(USER_ID);
        verify(converter, times(1)).toUserBO(userEntity);
    }

    @Test
    public void getAccountAccess() throws UserNotFoundException {
        UserEntity userEntity = readUserEntity(reader);
        UserBO userBO = readUserBO(reader);

        when(repository.findById(USER_ID)).thenReturn(Optional.of(userEntity));
        when(converter.toUserBO(userEntity)).thenReturn(userBO);

        List<AccountAccessBO> accAccess = userService.getAccountAccess(USER_ID);

        assertThat(accAccess.size(), is(4));
        assertThat(accAccess.get(0).getIban(), is(USER_IBAN));
        assertThat(accAccess.get(1).getIban(), is(USER_IBAN));
        assertThat(accAccess.get(0).getAccessType(), is(USER_ACC_ACCESS_TYPE_1));
        assertThat(accAccess.get(1).getAccessType(), is(USER_ACC_ACCESS_TYPE_2));

        verify(repository, times(1)).findById(USER_ID);
        verify(converter, times(1)).toUserBO(userEntity);
    }

    @Test(expected = UserNotFoundException.class)
    public void authorizeWithException() throws UserNotFoundException {

        when(repository.findFirstByLogin(USER_NON_EXISTING_LOGIN)).thenReturn(Optional.empty());

        userService.authorize(USER_NON_EXISTING_LOGIN, "SomePin");
    }

    @Test(expected = UserNotFoundException.class)
    public void getUserScaDataWithException() throws UserNotFoundException {

        when(repository.findById(USER_NON_EXISTING_ID)).thenReturn(Optional.empty());

        userService.getUserScaData(USER_NON_EXISTING_ID);
    }

    @Test(expected = UserNotFoundException.class)
    public void getAccountAccessWithException() throws UserNotFoundException {

        when(repository.findById(USER_NON_EXISTING_ID)).thenReturn(Optional.empty());

        userService.getAccountAccess(USER_NON_EXISTING_ID);
    }

    @Test
    public void getAccountAccessByUserLogin() throws UserNotFoundException {
        when(repository.findFirstByLogin(USER_LOGIN)).thenReturn(Optional.of(userEntity));
        when(converter.toUserBO(userEntity)).thenReturn(userBO);

        List<AccountAccessBO> access = userService.getAccountAccessByUserLogin(USER_LOGIN);

        assertThat(access.size(), is(4));

        assertThat(access.get(0).getId(), is("1"));
        assertThat(access.get(1).getId(), is("2"));

        assertThat(access.get(0).getIban(), is(USER_IBAN));
        assertThat(access.get(1).getIban(), is(USER_IBAN));

        assertThat(access.get(0).getAccessType(), is(USER_ACC_ACCESS_TYPE_1));
        assertThat(access.get(1).getAccessType(), is(USER_ACC_ACCESS_TYPE_2));


        assertThat(access.get(2).getId(), is("3"));
        assertThat(access.get(2).getAccessType(), is(AccessTypeBO.OWNER));
        assertThat(access.get(2).getIban(), is("1234567"));

        assertThat(access.get(3).getId(), is("4"));
        assertThat(access.get(3).getAccessType(), is(AccessTypeBO.READ));
        assertThat(access.get(3).getIban(), is("7777777"));

        verify(repository, times(1)).findFirstByLogin(USER_LOGIN);
        verify(converter, times(1)).toUserBO(userEntity);
    }

    @Test(expected = UserNotFoundException.class)
    public void getAccountAccessByUserLoginUserNotFound() throws UserNotFoundException {
        when(repository.findFirstByLogin(USER_LOGIN)).thenReturn(Optional.empty());
        userService.getAccountAccessByUserLogin(USER_LOGIN);
    }


        private UserBO readUserBO(ResourceReader reader) {
        try {
            return reader.getObjectFromFile("de/adorsys/ledgers/um/impl/service/user-BO.yml", UserBO.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private UserEntity readUserEntity(ResourceReader reader) {
        try {
            return reader.getObjectFromFile("de/adorsys/ledgers/um/impl/service/user-entity.yml", UserEntity.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
