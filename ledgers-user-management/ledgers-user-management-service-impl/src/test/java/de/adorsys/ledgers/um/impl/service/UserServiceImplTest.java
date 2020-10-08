package de.adorsys.ledgers.um.impl.service;

import de.adorsys.ledgers.um.api.domain.*;
import de.adorsys.ledgers.um.api.service.ScaUserDataService;
import de.adorsys.ledgers.um.db.domain.AccountAccess;
import de.adorsys.ledgers.um.db.domain.AisConsentEntity;
import de.adorsys.ledgers.um.db.domain.UserEntity;
import de.adorsys.ledgers.um.db.domain.UserRole;
import de.adorsys.ledgers.um.db.repository.AisConsentRepository;
import de.adorsys.ledgers.um.db.repository.UserRepository;
import de.adorsys.ledgers.um.impl.converter.AisConsentMapper;
import de.adorsys.ledgers.um.impl.converter.UserConverter;
import de.adorsys.ledgers.um.impl.converter.UserConverterTest;
import de.adorsys.ledgers.util.PasswordEnc;
import de.adorsys.ledgers.util.exception.UserManagementModuleException;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import pro.javatar.commons.reader.ResourceReader;
import pro.javatar.commons.reader.YamlReader;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
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

    private static final ResourceReader reader = YamlReader.getInstance();
    private static final UserConverter LOCAL_CONVERTER = Mappers.getMapper(UserConverter.class);

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
    private ScaUserDataService scaUserDataService;
    @Mock
    private AisConsentMapper aisConsentMapper;
    @Mock
    private AisConsentRepository consentRepository;


    @BeforeEach
    void setUp() {
        userEntity = readUserEntity();
        userBO = readUserBO();
    }

    @Test
    void create() {
        // Given
        when(repository.findByEmailOrLogin(any(), any())).thenReturn(Optional.empty());
        when(converter.toUserPO(any())).thenReturn(userEntity);
        when(passwordEnc.encode(any(), any())).thenReturn(USER_PIN);
        when(repository.save(any())).thenReturn(userEntity);
        when(converter.toUserBO(any())).thenReturn(userBO);

        // When
        UserBO user = userService.create(userBO);

        // Then
        assertNotNull(user);
        assertEquals(userBO, user);
        verify(repository, times(1)).save(userEntity);
    }

    @Test
    void create_userExist() {
        // Given
        when(repository.findByEmailOrLogin(any(), any())).thenReturn(Optional.of(userEntity));

        // Then
        assertThrows(UserManagementModuleException.class, () -> userService.create(userBO));
    }

    @Test
    void listUsers() {
        // Given
        when(repository.findAll(PageRequest.of(0, 15))).thenReturn(new PageImpl<UserEntity>(Collections.singletonList(userEntity)));
        when(converter.toUserBOList(any())).thenReturn(Collections.singletonList(userBO));

        // When
        List<UserBO> users = userService.listUsers(0, 15);

        // Then
        assertEquals(userBO, users.get(0));
        verify(repository, times(1)).findAll(PageRequest.of(0, 15));
    }

    @Test
    void findById() {
        // Given
        when(repository.findById(USER_ID)).thenReturn(Optional.of(userEntity));
        when(converter.toUserBO(any())).thenReturn(userBO);
        when(passwordEnc.encode(USER_ID, USER_PIN)).thenReturn(THE_ENCODED_VALUE);
        when(passwordEnc.verify(USER_ID, USER_PIN, THE_ENCODED_VALUE)).thenReturn(true);

        // When
        UserBO user = userService.findById(USER_ID);

        // Then
        assertThat(user.getId(), is(USER_ID));
        assertThat(user.getEmail(), is(USER_EMAIL));
        assertThat(user.getLogin(), is(USER_LOGIN));
        assertTrue(passwordEnc.verify(user.getId(), user.getPin(), passwordEnc.encode(USER_ID, USER_PIN)));
        verify(repository, times(1)).findById(USER_ID);
    }

    @Test
    void findByLogin() {
        // Given
        when(repository.findFirstByLogin(any())).thenReturn(Optional.ofNullable(userEntity));
        when(converter.toUserBO(any())).thenReturn(userBO);

        // When
        UserBO user = userService.findByLogin(USER_LOGIN);

        // Then
        assertNotNull(user);
        assertEquals(userBO, user);
        verify(repository, times(1)).findFirstByLogin(USER_LOGIN);
    }

    @Test
    void findByLogin_userNotFound() {
        // Given
        when(repository.findFirstByLogin(any())).thenThrow(UserManagementModuleException.builder().build());

        // Then
        assertThrows(UserManagementModuleException.class, () -> userService.findByLogin(USER_LOGIN));

    }

    @Test
    void updateScaData() throws IOException {
        // Given
        List<ScaUserDataBO> scaUserDataBOS = getScaUserData(ScaUserDataBO.class);

        when(repository.findFirstByLogin(USER_LOGIN)).thenReturn(Optional.ofNullable(userEntity));
        when(repository.save(userEntity)).thenReturn(userEntity);
        when(converter.toUserBO(any())).thenReturn(userBO);

        // When
        userService.updateScaData(scaUserDataBOS, USER_LOGIN);

        // Then
        verify(repository, times(1)).findFirstByLogin(USER_LOGIN);
        verify(repository, times(1)).save(userEntity);
    }

    @Test
    void updateScaDataUserNotFound() throws IOException {
        // Given
        List<ScaUserDataBO> scaUserDataBOS = getScaUserData(ScaUserDataBO.class);
        when(repository.findFirstByLogin(USER_LOGIN)).thenReturn(Optional.empty());

        // When
        assertThrows(UserManagementModuleException.class, () -> userService.updateScaData(scaUserDataBOS, USER_LOGIN));
    }

    @Test
    void updateAccountAccess() {
        // Given
        when(repository.findFirstByLogin(any())).thenReturn(Optional.of(userEntity));
        when(converter.toAccountAccessListEntity(any())).thenReturn(accountAccessList);
        when(repository.save(any())).thenReturn(userEntity);
        when(converter.toUserBO(any())).thenReturn(userBO);

        //when
        UserBO user = userService.updateAccountAccess(USER_LOGIN, accountAccessBOList);

        //then
        assertNotNull(user);
        assertEquals(userBO, user);
        verify(repository, times(1)).save(userEntity);
    }

    @Test
    void updateAccountAccess_userNotFound() {
        // Given
        when(repository.findFirstByLogin(any())).thenThrow(UserManagementModuleException.builder().build());

        // Then
        assertThrows(UserManagementModuleException.class, () -> userService.updateAccountAccess(USER_LOGIN, accountAccessBOList));
    }

    @Test
    void storeConsent() {
        // Given
        when(consentRepository.findById(any())).thenReturn(Optional.of(new AisConsentEntity()));
        when(aisConsentMapper.toAisConsentBO(any())).thenReturn(getAisConsentBO());

        // When
        AisConsentBO consent = userService.storeConsent(getAisConsentBO());

        // Then
        assertNotNull(consent);
        assertTrue(EqualsBuilder.reflectionEquals(getAisConsentBO(), consent));
        verify(consentRepository, times(1)).findById("id");
    }

    @Test
    void loadConsent() {
        // Given
        when(consentRepository.findById(any())).thenReturn(Optional.of(new AisConsentEntity()));
        when(aisConsentMapper.toAisConsentBO(any())).thenReturn(getAisConsentBO());

        // When
        AisConsentBO consent = userService.loadConsent(CONSENT_ID);

        // Then
        assertNotNull(consent);
        assertTrue(EqualsBuilder.reflectionEquals(getAisConsentBO(), consent));
        verify(consentRepository, times(1)).findById(CONSENT_ID);
    }

    @Test
    void loadConsent_consentNotFound() {
        // Given
        when(consentRepository.findById(any())).thenThrow(UserManagementModuleException.builder().build());

        // Then
        assertThrows(UserManagementModuleException.class, () -> userService.loadConsent(CONSENT_ID));
    }

    @Test
    void findByBranchAndUserRolesIn() {
        // Given
        when(repository.findBranchIdsByMultipleParameters(any(), any(), any(), eq(UserRole.STAFF))).thenReturn(Collections.singletonList(getBranchEntity()));
        when(converter.toUserBOList(any())).thenAnswer(a -> LOCAL_CONVERTER.toUserBOList(a.getArgument(0)));
        when(converter.toUserRole(any())).thenReturn(Collections.singletonList(UserRole.CUSTOMER));
        when(converter.toUserExtendedBO(any(), anyString())).thenAnswer(a -> LOCAL_CONVERTER.toUserExtendedBO(a.getArgument(0), a.getArgument(1)));
        when(repository.findByBranchInAndLoginContainingAndUserRolesInAndBlockedInAndSystemBlockedFalse(any(), any(), any(), any(), any())).thenReturn(new PageImpl<>(Collections.singletonList(userEntity)));

        // When
        Page<UserExtendedBO> user = userService.findUsersByMultipleParamsPaged("", USER_BRANCH, "", "", Collections.singletonList(UserRoleBO.CUSTOMER), false, null);

        // Then
        assertNotNull(user.getContent().get(0));
        assertEquals(USER_BRANCH, user.getContent().get(0).getBranchLogin());
        verify(converter, times(1)).toUserRole(Collections.singletonList(UserRoleBO.CUSTOMER));
    }

    private UserEntity getBranchEntity() {
        UserEntity entity = new UserEntity();
        entity.setId(USER_BRANCH);
        entity.setBranch(USER_BRANCH);
        entity.setLogin(USER_BRANCH);
        return entity;
    }

    @Test
    void countUsersByBranch() {
        // Given
        when(repository.countByBranch(any())).thenReturn(1);

        // When
        int result = userService.countUsersByBranch(USER_BRANCH);

        // Then
        assertEquals(1, result);
        verify(repository, times(1)).countByBranch(USER_BRANCH);
    }

    @Test
    void updateUser() {
        // Given
        when(converter.toUserPO(any())).thenReturn(userEntity);
        when(repository.findById(any())).thenReturn(Optional.of(userEntity));
        when(repository.save(any())).thenReturn(userEntity);
        when(converter.toUserBO(any())).thenReturn(userBO);

        // When
        UserBO user = userService.updateUser(userBO);

        // Then
        assertNotNull(user);
        assertEquals(userBO, user);
        verify(repository, times(2)).findById(USER_ID);
    }

    @Test
    void updateUser_duplicatingScaMethods() {
        // Given
        userBO.getScaUserData().add(getScaUserDataBO());

        // Then
        assertThrows(UserManagementModuleException.class, () -> userService.updateUser(userBO));
    }

    @Test
    void findUsersByIban() {
        // Given
        when(repository.findUsersByIban(anyString())).thenReturn(Collections.singletonList(userEntity));
        when(converter.toUserBOList(any())).thenReturn(Collections.singletonList(userBO));

        // When
        List<UserBO> users = userService.findUsersByIban(USER_IBAN);

        // Then
        assertNotNull(users);
        assertEquals(userBO, users.get(0));
        verify(converter, times(1)).toUserBOList(Collections.singletonList(userEntity));
    }

    @Test
    void findOwnersByIban() {
        // Given
        when(repository.findOwnersByIban(anyString(), any())).thenReturn(Collections.emptyList());
        when(converter.toUserBOList(any())).thenReturn(Collections.singletonList(getUserBO()));
        List<UserBO> result = userService.findOwnersByIban(USER_IBAN);

        // Then
        assertEquals(Collections.singletonList(getUserBO()), result);
    }

    @Test
    void findOwnersByAccountId() {
        // Given
        when(repository.findOwnersByAccountId(anyString(), any())).thenReturn(Collections.emptyList());
        when(converter.toUserBOList(any())).thenReturn(Collections.singletonList(getUserBO()));
        List<UserBO> result = userService.findOwnersByAccountId("accountId");

        // Then
        assertEquals(Collections.singletonList(getUserBO()), result);
    }

    @Test
    void updatePassword() {
        when(repository.findById(USER_ID)).thenReturn(Optional.of(new UserEntity()));
        when(passwordEnc.encode(any(), any())).thenReturn("encrypted");
        userService.updatePassword(USER_ID, "password");
        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(repository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getPin(), is("encrypted"));
    }

    @Test
    void updatePassword_user_not_found() {
        when(repository.findById(USER_ID)).thenReturn(Optional.empty());
        assertThrows(UserManagementModuleException.class, () -> userService.updatePassword(USER_ID, "password"));
    }

    @Test
    void findUserLoginsByBranch() {
        // Given
        when(repository.findByBranch(USER_BRANCH))
                .thenReturn(Collections.singletonList(getBranchEntity()));
        // When
        List<String> logins = userService.findUserLoginsByBranch(USER_BRANCH);

        // Then
        assertEquals(USER_BRANCH, logins.get(0));
    }

    @Test
    void findUsersByBranchAndCreatedAfter() {
        // Given
        List<UserEntity> userEntities = Collections.singletonList(readUserEntity());
        when(repository.findByBranchAndCreatedAfter(USER_BRANCH, LocalDateTime.MIN))
                .thenReturn(userEntities);
        when(converter.toUserBOList(userEntities))
                .thenReturn(Collections.singletonList(getUserBO()));
        // When
        List<UserBO> actual = userService.findUsersByBranchAndCreatedAfter(USER_BRANCH, LocalDateTime.MIN);

        // Then
        assertNotNull(actual);
        assertEquals(getUserBO(), actual.get(0));
    }

    private UserBO getUserBO() {
        return new UserBO("login", "email", "pin");
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

    @Test
    void setBranchBlockedStatus_system_block() {
        userService.setBranchBlockedStatus(USER_BRANCH, true, true);
        verify(repository, times(1)).updateSystemBlockedStatus(USER_BRANCH, true);
    }

    @Test
    void setBranchBlockedStatus_regular_block() {
        userService.setBranchBlockedStatus(USER_BRANCH, false, true);
        verify(repository, times(1)).updateBlockedStatus(USER_BRANCH, true);
    }
}