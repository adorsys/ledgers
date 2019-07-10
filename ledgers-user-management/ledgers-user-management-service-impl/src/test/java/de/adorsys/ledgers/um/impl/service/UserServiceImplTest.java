package de.adorsys.ledgers.um.impl.service;

import de.adorsys.ledgers.um.api.domain.BearerTokenBO;
import de.adorsys.ledgers.um.api.domain.ScaUserDataBO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.domain.UserRoleBO;
import de.adorsys.ledgers.um.api.exception.UserManagementModuleException;
import de.adorsys.ledgers.um.db.domain.ScaUserDataEntity;
import de.adorsys.ledgers.um.db.domain.UserEntity;
import de.adorsys.ledgers.um.db.repository.UserRepository;
import de.adorsys.ledgers.um.impl.converter.UserConverter;
import de.adorsys.ledgers.util.PasswordEnc;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import pro.javatar.commons.reader.ResourceReader;
import pro.javatar.commons.reader.YamlReader;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceImplTest {

    @InjectMocks
    public UserServiceImpl userService;

    @Mock
    private UserRepository repository;

    @Mock
    private UserConverter converter;

    @Mock
    private PasswordEnc passwordEnc;

    @Mock
    private HashMacSecretSource secretSource;

    @Mock
    private BearerTokenService bearerTokenService;

    private ResourceReader reader = YamlReader.getInstance();

    private static final String USER_ID = "SomeUniqueID";
    private static final String USER_EMAIL = "vne@adorsys.de";
    private static final String USER_LOGIN = "vne";
    private static final String USER_PIN = "12345678";
    private static final String THE_ENCODED_VALUE = "25d55ad283aa400af464c76d713c07ad";
    private static final String USER_NON_EXISTING_LOGIN = "NonExistingLogin";
    private UserEntity userEntity;
    private UserBO userBO;

    @Before
    public void setUp() {
        userEntity = readUserEntity();
        userBO = readUserBO();
    }

    @Test
    public void updateScaData() throws IOException {
        List<ScaUserDataBO> scaUserDataBOS = getScaUserData(ScaUserDataBO.class);
        List<ScaUserDataEntity> scaUserDataEntities = getScaUserData(ScaUserDataEntity.class);

        when(repository.findFirstByLogin(USER_LOGIN)).thenReturn(Optional.ofNullable(userEntity));
        when(repository.save(userEntity)).thenReturn(userEntity);

        userService.updateScaData(scaUserDataBOS, USER_LOGIN);

        verify(repository, times(1)).findFirstByLogin(USER_LOGIN);
        verify(repository, times(1)).save(userEntity);
    }

    @Test(expected = UserManagementModuleException.class)
    public void updateScaDataUserNotFound() throws IOException {
        List<ScaUserDataBO> scaUserDataBOS = getScaUserData(ScaUserDataBO.class);

        when(repository.findFirstByLogin(USER_LOGIN)).thenReturn(Optional.empty());

        userService.updateScaData(scaUserDataBOS, USER_LOGIN);
    }

    private <T> List<T> getScaUserData(Class<T> clazz) throws IOException {
        return reader.getListFromInputStream(getClass().getResourceAsStream("sca-user-methods.yml"), clazz);
    }

    @Test(expected = UserManagementModuleException.class)
    public void authorizeWithLoginAndPin() {

        when(repository.findFirstByLogin(USER_LOGIN)).thenReturn(Optional.empty());
        BearerTokenBO bearerTokenBO = userService.authorise(USER_LOGIN, USER_PIN, UserRoleBO.CUSTOMER, null, null);

        assertNull(bearerTokenBO);
    }

    @Test
    public void findById() {

        when(repository.findById(USER_ID)).thenReturn(Optional.of(userEntity));
        when(converter.toUserBO(any())).thenReturn(userBO);
        when(passwordEnc.encode(USER_ID, USER_PIN)).thenReturn(THE_ENCODED_VALUE);
        when(passwordEnc.verify(USER_ID, USER_PIN, THE_ENCODED_VALUE)).thenReturn(true);

        UserBO user = userService.findById(USER_ID);

        assertThat(user.getId(), is(USER_ID));
        assertThat(user.getEmail(), is(USER_EMAIL));
        assertThat(user.getLogin(), is(USER_LOGIN));
        assertTrue(passwordEnc.verify(user.getId(), user.getPin(), passwordEnc.encode(USER_ID, USER_PIN)));

        verify(repository, times(1)).findById(USER_ID);
    }

    @Test(expected = UserManagementModuleException.class)
    public void authorizeWithException() {

        when(repository.findFirstByLogin(USER_NON_EXISTING_LOGIN)).thenReturn(Optional.empty());

        userService.authorise(USER_NON_EXISTING_LOGIN, "SomePin", UserRoleBO.CUSTOMER, null, null);
    }

    private UserBO readUserBO() {
        try {
            return reader.getObjectFromResource(getClass(), "user-BO.yml", UserBO.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private UserEntity readUserEntity() {
        try {
            return reader.getObjectFromResource(getClass(), "user-entity.yml", UserEntity.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
