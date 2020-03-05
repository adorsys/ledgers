package de.adorsys.ledgers.um.impl.service;

import de.adorsys.ledgers.um.api.domain.*;
import de.adorsys.ledgers.um.api.service.ScaUserDataService;
import de.adorsys.ledgers.um.db.domain.AccountAccess;
import de.adorsys.ledgers.um.db.domain.AisConsentEntity;
import de.adorsys.ledgers.um.db.domain.UserRole;
import de.adorsys.ledgers.um.db.repository.AisConsentRepository;
import de.adorsys.ledgers.um.impl.converter.AisConsentMapper;
import de.adorsys.ledgers.um.impl.converter.UserConverterTest;
import de.adorsys.ledgers.util.exception.UserManagementModuleException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import pro.javatar.commons.reader.ResourceReader;
import pro.javatar.commons.reader.YamlReader;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceImplTest {
    private static final List<AccountAccess> accountAccessList = readListYml(AccountAccess.class, "account-access.yml");
    private static final List<AccountAccessBO> accountAccessBOList = readListYml(AccountAccessBO.class, "account-access.yml");
    private static final String USER_ID = "SomeUniqueID";
    private static final String CONSENT_ID = "consentId";
    private static final String USER_EMAIL = "test@mail.de";
    private static final String USER_LOGIN = "test";
    private static final String USER_PIN = "12345678";
    private static final String USER_BRANCH = "userBranch";
    private static final String USER_IBAN = "DE12345678";
    private static final String THE_ENCODED_VALUE = "25d55ad283aa400af464c76d713c07ad";
    private UserEntity userEntity;
    private UserBO userBO;

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
    @Mock
    private ScaUserDataService scaUserDataService;
    @Mock
    private AisConsentMapper aisConsentMapper;
    @Mock
    private AisConsentRepository consentRepository;

    private ResourceReader reader = YamlReader.getInstance();

    @Before
    public void setUp() {
        userEntity = readUserEntity();
        userBO = readUserBO();
    }

    @Test
    public void create() {
        //given
        when(repository.findByEmailOrLogin(any(), any())).thenReturn(Optional.empty());
        when(converter.toUserPO(any())).thenReturn(userEntity);
        when(passwordEnc.encode(any(), any())).thenReturn(USER_PIN);
        when(repository.save(any())).thenReturn(userEntity);
        when(converter.toUserBO(any())).thenReturn(userBO);

        //when
        UserBO user = userService.create(userBO);

        //then
        assertThat(user).isNotNull();
        assertEquals(userBO, user);
        verify(repository, times(1)).save(userEntity);
    }

    @Test(expected = UserManagementModuleException.class)
    public void create_userExist() {
        //given
        when(repository.findByEmailOrLogin(any(), any())).thenReturn(Optional.of(userEntity));

        //when
        userService.create(userBO);
    }

    @Test
    public void listUsers() {
        //given
        when(repository.findAll(PageRequest.of(0, 15))).thenReturn(new PageImpl<UserEntity>(Collections.singletonList(userEntity)));
        when(converter.toUserBOList(any())).thenReturn(Collections.singletonList(userBO));

        //when
        List<UserBO> users = userService.listUsers(0, 15);

        //then;
        assertEquals(userBO, users.get(0));
        verify(repository, times(1)).findAll(PageRequest.of(0, 15));
    }

    @Test
    public void findById() {
        //given
        when(repository.findById(USER_ID)).thenReturn(Optional.of(userEntity));
        when(converter.toUserBO(any())).thenReturn(userBO);
        when(passwordEnc.encode(USER_ID, USER_PIN)).thenReturn(THE_ENCODED_VALUE);
        when(passwordEnc.verify(USER_ID, USER_PIN, THE_ENCODED_VALUE)).thenReturn(true);

        //when
        UserBO user = userService.findById(USER_ID);

        //then
        assertThat(user.getId(), is(USER_ID));
        assertThat(user.getEmail(), is(USER_EMAIL));
        assertThat(user.getLogin(), is(USER_LOGIN));
        assertTrue(passwordEnc.verify(user.getId(), user.getPin(), passwordEnc.encode(USER_ID, USER_PIN)));
        verify(repository, times(1)).findById(USER_ID);
    }

    @Test
    public void findByLogin() {
        //given
        when(repository.findFirstByLogin(any())).thenReturn(Optional.ofNullable(userEntity));
        when(converter.toUserBO(any())).thenReturn(userBO);

        //when
        UserBO user = userService.findByLogin(USER_LOGIN);

        //then
        assertThat(user).isNotNull();
        assertEquals(userBO, user);
        verify(repository, times(1)).findFirstByLogin(USER_LOGIN);
    }

    @Test(expected = UserManagementModuleException.class)
    public void findByLogin_userNotFound() {
        //given
        when(repository.findFirstByLogin(any())).thenThrow(UserManagementModuleException.builder().build());

        //when
        userService.findByLogin(USER_LOGIN);
    }

    @Test
    public void updateScaData() throws IOException {
        //given
        List<ScaUserDataBO> scaUserDataBOS = getScaUserData(ScaUserDataBO.class);

        when(repository.findFirstByLogin(USER_LOGIN)).thenReturn(Optional.ofNullable(userEntity));
        when(repository.save(userEntity)).thenReturn(userEntity);
        when(converter.toUserBO(any())).thenReturn(userBO);

        //when
        userService.updateScaData(scaUserDataBOS, USER_LOGIN);

        //then
        verify(repository, times(1)).findFirstByLogin(USER_LOGIN);
        verify(repository, times(1)).save(userEntity);
    }

    @Test(expected = UserManagementModuleException.class)
    public void updateScaDataUserNotFound() throws IOException {
        //given
        List<ScaUserDataBO> scaUserDataBOS = getScaUserData(ScaUserDataBO.class);
        when(repository.findFirstByLogin(USER_LOGIN)).thenReturn(Optional.empty());

        //when
        userService.updateScaData(scaUserDataBOS, USER_LOGIN);
    }

    @Test
    public void updateAccountAccess() {
        //given
        when(repository.findFirstByLogin(any())).thenReturn(Optional.of(userEntity));
        when(converter.toAccountAccessListEntity(any())).thenReturn(accountAccessList);
        when(repository.save(any())).thenReturn(userEntity);
        when(converter.toUserBO(any())).thenReturn(userBO);

        //when
        UserBO user = userService.updateAccountAccess(USER_LOGIN, accountAccessBOList);

        //then
        assertThat(user).isNotNull();
        assertEquals(userBO, user);
        verify(repository, times(1)).save(userEntity);
    }

    @Test(expected = UserManagementModuleException.class)
    public void updateAccountAccess_userNotFound() {
        //given
        when(repository.findFirstByLogin(any())).thenThrow(UserManagementModuleException.builder().build());

        //when
        userService.updateAccountAccess(USER_LOGIN, accountAccessBOList);
    }

    @Test
    public void storeConsent() {
        //given
        when(consentRepository.findById(any())).thenReturn(Optional.of(new AisConsentEntity()));
        when(aisConsentMapper.toAisConsentBO(any())).thenReturn(getAisConsentBO());

        //when
        AisConsentBO consent = userService.storeConsent(getAisConsentBO());

        //then
        assertThat(consent).isNotNull();
        assertThat(consent).isEqualToComparingFieldByFieldRecursively(getAisConsentBO());
        verify(consentRepository, times(1)).findById("id");
    }

    @Test
    public void loadConsent() {
        //given
        when(consentRepository.findById(any())).thenReturn(Optional.of(new AisConsentEntity()));
        when(aisConsentMapper.toAisConsentBO(any())).thenReturn(getAisConsentBO());

        //when
        AisConsentBO consent = userService.loadConsent(CONSENT_ID);

        //then
        assertThat(consent).isNotNull();
        assertThat(consent).isEqualToComparingFieldByFieldRecursively(getAisConsentBO());
        verify(consentRepository, times(1)).findById(CONSENT_ID);
    }

    @Test(expected = UserManagementModuleException.class)
    public void loadConsent_consentNotFound() {
        //given
        when(consentRepository.findById(any())).thenThrow(UserManagementModuleException.builder().build());

        //when
        userService.loadConsent(CONSENT_ID);
    }

    @Test
    public void findByBranchAndUserRolesIn() {
        //given
        when(converter.toUserRole(any())).thenReturn(Collections.singletonList(UserRole.CUSTOMER));
        when(converter.toUserBO(any())).thenReturn(userBO);
        when(repository.findByBranchAndUserRolesInAndLoginContaining(any(), any(), any(), any())).thenReturn(new PageImpl<UserEntity>(Collections.singletonList(userEntity)));

        //when
        Page<UserBO> user = userService.findByBranchAndUserRolesIn(USER_BRANCH, Collections.singletonList(UserRoleBO.CUSTOMER), "", null);

        //then
        assertThat(user.getContent().get(0)).isNotNull();
        assertEquals(userBO, user.getContent().get(0));
        verify(converter, times(1)).toUserRole(Collections.singletonList(UserRoleBO.CUSTOMER));
    }

    @Test
    public void countUsersByBranch() {
        //given
        when(repository.countByBranch(any())).thenReturn(1);

        //when
        int result = userService.countUsersByBranch(USER_BRANCH);

        //then
        assertEquals(1, result);
        verify(repository, times(1)).countByBranch(USER_BRANCH);
    }

    @Test
    public void updateUser() {
        //given
        when(converter.toUserPO(any())).thenReturn(userEntity);
        when(repository.findById(any())).thenReturn(Optional.of(userEntity));
        when(repository.save(any())).thenReturn(userEntity);
        when(converter.toUserBO(any())).thenReturn(userBO);

        //when
        UserBO user = userService.updateUser(userBO);

        //then
        assertThat(user).isNotNull();
        assertThat(user).isEqualToComparingFieldByFieldRecursively(userBO);
        verify(repository, times(2)).findById(USER_ID);
    }

    @Test(expected = UserManagementModuleException.class)
    public void updateUser_duplicatingScaMethods() {
        //given
        userBO.getScaUserData().add(getScaUserDataBO());
        when(converter.toUserPO(any())).thenReturn(userEntity);
        when(repository.findById(any())).thenReturn(Optional.of(userEntity));
        when(repository.save(any())).thenReturn(userEntity);
        when(converter.toUserBO(any())).thenReturn(userBO);

        //when
        userService.updateUser(userBO);
    }

    @Test
    public void findUsersByIban() {
        //given
        when(repository.findUsersByIban(anyString())).thenReturn(Collections.singletonList(userEntity));
        when(converter.toUserBOList(any())).thenReturn(Collections.singletonList(userBO));

        //when
        List<UserBO> users = userService.findUsersByIban(USER_IBAN);

        //then
        assertThat(users).isNotNull();
        assertThat(users.get(0)).isEqualToComparingFieldByFieldRecursively(userBO);
        verify(converter, times(1)).toUserBOList(Collections.singletonList(userEntity));
    }

    private <T> List<T> getScaUserData(Class<T> clazz) throws IOException {
        return reader.getListFromInputStream(getClass().getResourceAsStream("sca-user-methods.yml"), clazz);
    }

    private static <T> List<T> readListYml(Class<T> aClass, String fileName) {
        try {
            return YamlReader.getInstance().getListFromResource(UserConverterTest.class, fileName, aClass);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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

    private AisConsentBO getAisConsentBO() {
        return new AisConsentBO("id", "userId", "tppId", 4, new AisAccountAccessInfoBO(), LocalDate.now().plusDays(5), false);
    }

    private ScaUserDataBO getScaUserDataBO() {
        return new ScaUserDataBO("3", ScaMethodTypeBO.EMAIL, "test@mail.de", false, "staticTan", true);
    }
}