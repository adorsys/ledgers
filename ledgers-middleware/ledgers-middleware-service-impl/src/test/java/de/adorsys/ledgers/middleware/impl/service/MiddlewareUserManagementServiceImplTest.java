package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.deposit.api.domain.DepositAccountDetailsBO;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.middleware.api.domain.account.AccountIdentifierTypeTO;
import de.adorsys.ledgers.middleware.api.domain.account.AccountReferenceTO;
import de.adorsys.ledgers.middleware.api.domain.account.AdditionalAccountInformationTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.um.*;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareModuleException;
import de.adorsys.ledgers.middleware.impl.converter.AdditionalAccountInformationMapper;
import de.adorsys.ledgers.middleware.impl.converter.PageMapper;
import de.adorsys.ledgers.middleware.impl.converter.UserMapper;
import de.adorsys.ledgers.um.api.domain.AccessTypeBO;
import de.adorsys.ledgers.um.api.domain.AccountAccessBO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.service.UserService;
import de.adorsys.ledgers.util.domain.CustomPageImpl;
import de.adorsys.ledgers.util.domain.CustomPageableImpl;
import de.adorsys.ledgers.util.exception.UserManagementModuleException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import pro.javatar.commons.reader.YamlReader;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MiddlewareUserManagementServiceImplTest {
    private static final String USER_ID = "SomeUniqueID";
    private static final String BRANCH_ID = "Nuremberg";
    private static final String USER_LOGIN = "test";
    private static final String ACCOUNT_ID = "accountId";
    private static final String USER_BRANCH = "userBranch";
    private static final String USER_IBAN = "DE12345678";
    private static final String AUTHORIZATION_ID = "authorizationId";
    private static final String AUTH_CODE = "123456";
    private static final String SCA_METHOD_ID = "scaMethodId";
    private static final String SCA_ID = "scaId";
    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Currency USD = Currency.getInstance("USD");
    private static final String ANOTHER_USER_ID = "other user";

    @InjectMocks
    private MiddlewareUserManagementServiceImpl middlewareUserService;
    @Mock
    private UserService userService;
    @Mock
    private DepositAccountService depositAccountService;
    @Mock
    private AccessService accessService;
    @Mock
    private UserMapper userTOMapper;
    @Mock
    private PageMapper pageMapper;

    private static UserBO userBO = null;
    private static UserTO userTO = null;

    @BeforeAll
    static void before() {
        userBO = readYml(UserBO.class, "user.yml");
        userTO = readYml(UserTO.class, "user.yml");
    }

    @Test
    void create() {
        // Given
        when(userService.create(any())).thenReturn(userBO);

        // When
        UserTO user = middlewareUserService.create(userTO);

        // Then
        assertNotNull(user);
        assertThat(user).isEqualToComparingFieldByField(userTO);
        verify(userService, times(1)).create(userBO);
    }

    @Test
    void findById() {
        // Given
        when(userService.findById(any())).thenReturn(userBO);

        // When
        UserTO user = middlewareUserService.findById(USER_ID);

        // Then
        assertNotNull(user);
        assertThat(user).isEqualToComparingFieldByField(userTO);
        verify(userService, times(1)).findById(USER_ID);
    }

    @Test
    void findByUserLoginUserNotFound() {
        // Given
        String login = "spe@adorsys.com.ua";
        when(userService.findByLogin(login)).thenThrow(UserManagementModuleException.builder().build());

        // Then
        assertThrows(UserManagementModuleException.class, () -> middlewareUserService.findByUserLogin(login));
    }

    @Test
    void updateScaMethods() {
        // Given
        String userLogin = "userLogin";
        when(userService.updateScaData(userBO.getScaUserData(), userLogin)).thenReturn(userBO);

        // When
        middlewareUserService.updateScaData(userLogin, userTO.getScaUserData());

        // Then
        verify(userService, times(1)).updateScaData(userBO.getScaUserData(), userLogin);
    }

    @Test
    void updateAccountAccess() {
        // Given
        when(depositAccountService.getAccountDetailsByIbanAndCurrency(any(), any(), any(), anyBoolean())).thenReturn(getDepositAccountDetailsBO());
        when(userService.findById(any())).thenReturn(userBO);
        when(accessService.userHasAccessToAccount(any(), any())).thenReturn(true);

        // When
        middlewareUserService.updateAccountAccess(buildScaInfoTO(), USER_ID, getAccessTO());

        // Then
        verify(userService, times(2)).findById(USER_ID);
    }

    @Test
    void updateAccountAccess_wrong_branch_access() {
        // Given
        when(depositAccountService.getAccountDetailsByIbanAndCurrency(any(), any(), any(), anyBoolean())).thenReturn(getDepositAccountDetailsBO());
        when(userService.findById(ANOTHER_USER_ID)).thenReturn(getUser(ANOTHER_USER_ID));
        when(userService.findById(USER_ID)).thenReturn(userBO);
        when(accessService.userHasAccessToAccount(any(), any())).thenReturn(true);

        // When
        assertThrows(MiddlewareModuleException.class, () -> middlewareUserService.updateAccountAccess(buildScaInfoTO(), ANOTHER_USER_ID, getAccessTO()));

        // Then
        verify(userService, times(1)).findById(USER_ID);
    }

    @Test
    void updateAccountAccess_accountNotEnabled() {
        // Given
        DepositAccountDetailsBO accountDetails = getDepositAccountDetailsBO();
        accountDetails.getAccount().setBlocked(true);
        when(depositAccountService.getAccountDetailsByIbanAndCurrency(any(), any(), any(), anyBoolean())).thenReturn(accountDetails);

        // Then
        assertThrows(MiddlewareModuleException.class, () -> middlewareUserService.updateAccountAccess(buildScaInfoTO(), USER_ID, getAccessTO()));
    }

    @Test
    void updateAccountAccess_noAccessToAccount() {
        // Given
        when(depositAccountService.getAccountDetailsByIbanAndCurrency(any(), any(), any(), anyBoolean())).thenReturn(getDepositAccountDetailsBO());
        when(userService.findById(any())).thenReturn(userBO);
        when(accessService.userHasAccessToAccount(any(), any())).thenReturn(false);

        // Then
        assertThrows(MiddlewareModuleException.class, () -> middlewareUserService.updateAccountAccess(buildScaInfoTO(), USER_ID, getAccessTO()));
    }

    @Test
    void listUsers() {
        // Given
        when(userService.listUsers(anyInt(), anyInt())).thenReturn(Collections.singletonList(userBO));

        // When
        middlewareUserService.listUsers(0, 15);

        // Then
        verify(userService, times(1)).listUsers(0, 15);
    }

    @Test
    void getUsersByBranchAndRoles() {
        // Given
        when(userService.findByBranchAndUserRolesIn(any(), any(), any(), any())).thenReturn(new PageImpl<>(Collections.singletonList(userBO)));
        when(pageMapper.toCustomPageImpl(any())).thenReturn(getCustomPageImpl());

        // When
        CustomPageImpl<UserTO> users = middlewareUserService.getUsersByBranchAndRoles(USER_BRANCH, Collections.singletonList(UserRoleTO.CUSTOMER), "", getCustomPageableImpl());

        // Then
        assertNotNull(users.getContent().get(0));
        assertThat(users.getContent().get(0)).isEqualToComparingFieldByField(userTO);
    }

    @Test
    void countUsersByBranch() {
        // Given
        when(userService.countUsersByBranch(any())).thenReturn(2);

        // When
        int result = middlewareUserService.countUsersByBranch(USER_BRANCH);

        // Then
        assertEquals(2, result);
        verify(userService, times(1)).countUsersByBranch(USER_BRANCH);
    }

    @Test
    void updateUser() {
        // Given
        UserTO user = userTO;
        user.setId(USER_ID);
        when(userService.findById(any())).thenReturn(userBO);
        when(userService.updateUser(any())).thenReturn(userBO);

        // When
        UserTO result = middlewareUserService.updateUser(BRANCH_ID, user);

        // Then
        assertNotNull(result);
        verify(userService, times(1)).findById(USER_ID);
    }

    @Test
    void updateUser_userIdNull() {
        // Then
        assertThrows(MiddlewareModuleException.class, () -> middlewareUserService.updateUser(BRANCH_ID, userTO));
    }

    @Test
    void updateUser_branchesNotMatch() {
        // Given
        UserTO user = userTO;
        user.setId(USER_ID);
        when(userService.findById(any())).thenReturn(userBO);

        // Then
        assertThrows(MiddlewareModuleException.class, () -> middlewareUserService.updateUser("anotherBranch", user));
    }

    @Test
    void getSCAMethods() {
        // Given
        String userLogin = "spe@adorsys.com.ua";
        when(userService.findByLogin(userLogin)).thenReturn(userBO);

        // When
        UserTO user = middlewareUserService.findByUserLogin(userLogin);

        // Then
        assertEquals(2, user.getScaUserData().size());
        assertThat(user.getScaUserData().get(0).getScaMethod()).isEqualByComparingTo(ScaMethodTypeTO.EMAIL);
        assertEquals("spe@adorsys.com.ua", user.getScaUserData().get(0).getMethodValue());
        assertEquals(ScaMethodTypeTO.MOBILE, user.getScaUserData().get(1).getScaMethod());
        assertEquals("+380933686868", user.getScaUserData().get(1).getMethodValue());
        verify(userService, times(1)).findByLogin(userLogin);
    }

    @Test
    void checkMultilevelScaRequired_multilevelNotEnabled() throws NoSuchFieldException {
        // Given
        FieldSetter.setField(middlewareUserService, middlewareUserService.getClass().getDeclaredField("multilevelScaEnable"), false);

        // When
        boolean result = middlewareUserService.checkMultilevelScaRequired(USER_LOGIN, USER_IBAN);

        // Then
        assertFalse(result);
    }

    @Test
    void checkMultilevelScaRequired_noAccess() throws NoSuchFieldException {
        // Given
        FieldSetter.setField(middlewareUserService, middlewareUserService.getClass().getDeclaredField("multilevelScaEnable"), true);
        when(userService.findByLogin(any())).thenReturn(userBO);

        // Then
        assertThrows(MiddlewareModuleException.class, () -> middlewareUserService.checkMultilevelScaRequired(USER_LOGIN, ""));
    }

    @Test
    void checkMultilevelScaRequired() throws NoSuchFieldException {
        // Given
        FieldSetter.setField(middlewareUserService, middlewareUserService.getClass().getDeclaredField("multilevelScaEnable"), true);
        when(userService.findByLogin(any())).thenReturn(userBO);
        when(accessService.resolveScaWeightByDebtorAccount(any(), any())).thenReturn(100);

        // When
        boolean result = middlewareUserService.checkMultilevelScaRequired(USER_LOGIN, "1234567");

        // Then
        assertFalse(result);
    }

    @Test
    void checkMultilevelScaRequired_no_multilevel() throws NoSuchFieldException {
        // Given
        FieldSetter.setField(middlewareUserService, middlewareUserService.getClass().getDeclaredField("multilevelScaEnable"), true);
        when(userService.findByLogin(any())).thenReturn(getUser(null));

        // When
        boolean response = middlewareUserService.checkMultilevelScaRequired("some_login", getReferences("1"));

        // Then
        assertFalse(response);
    }

    @Test
    void checkMultilevelScaRequired_empty_list() throws NoSuchFieldException {
        // Given
        FieldSetter.setField(middlewareUserService, middlewareUserService.getClass().getDeclaredField("multilevelScaEnable"), true);
        when(userService.findByLogin(any())).thenReturn(getUser(null));

        // When
        boolean response = middlewareUserService.checkMultilevelScaRequired("some_login", new ArrayList<>());

        // Then
        assertTrue(response);
    }

    @Test
    void checkMultilevelScaRequired_2_acc_with_mlsca() throws NoSuchFieldException {
        // Given
        FieldSetter.setField(middlewareUserService, middlewareUserService.getClass().getDeclaredField("multilevelScaEnable"), true);
        when(userService.findByLogin(any())).thenReturn(getUser(null));

        // When
        boolean response = middlewareUserService.checkMultilevelScaRequired("some_login", getReferences("1", "2"));

        // Then
        assertTrue(response);
    }

    @Test
    void checkMultilevelScaRequired_1_acc_no_curr_with_mlsca() throws NoSuchFieldException {
        // Given
        FieldSetter.setField(middlewareUserService, middlewareUserService.getClass().getDeclaredField("multilevelScaEnable"), true);
        when(userService.findByLogin(any())).thenReturn(getUser(null));

        // When
        boolean response = middlewareUserService.checkMultilevelScaRequired("some_login", Collections.singletonList(getReference("1", null)));

        // Then
        assertTrue(response);
    }

    @Test
    void checkMultilevelScaRequired_acc_not_match() throws NoSuchFieldException {
        // Given
        FieldSetter.setField(middlewareUserService, middlewareUserService.getClass().getDeclaredField("multilevelScaEnable"), true);
        when(userService.findByLogin(any())).thenReturn(getUser(null));

        // Then
        assertThrows(MiddlewareModuleException.class, () -> middlewareUserService.checkMultilevelScaRequired("some_login", getReferences("1", "3")));
    }

    @Test
    void checkMultilevelScaRequired_multilevel_false() throws NoSuchFieldException {
        // Given
        FieldSetter.setField(middlewareUserService, middlewareUserService.getClass().getDeclaredField("multilevelScaEnable"), false);

        // When
        boolean response = middlewareUserService.checkMultilevelScaRequired("some_login", getReferences("1", "2"));

        // Then
        assertFalse(response);
    }

    @Test
    void getAdditionalInformation() throws NoSuchFieldException {
        FieldSetter.setField(middlewareUserService, middlewareUserService.getClass().getDeclaredField("additionalInfoMapper"), Mappers.getMapper(AdditionalAccountInformationMapper.class));
        when(userService.findOwnersByIban(anyString())).thenReturn(Collections.singletonList(getUser(null)));
        List<AdditionalAccountInformationTO> result = middlewareUserService.getAdditionalInformation(new ScaInfoTO(), AccountIdentifierTypeTO.IBAN, ACCOUNT_ID);
        assertThat(result).isEqualTo(Collections.singletonList(getAdditionalInfo()));
    }

    private AdditionalAccountInformationTO getAdditionalInfo() {
        AdditionalAccountInformationTO to = new AdditionalAccountInformationTO();
        to.setAccountOwnerName(USER_LOGIN);
        to.setScaWeight(100);
        return to;
    }

    private static <T> T readYml(Class<T> aClass, String fileName) {
        try {
            return YamlReader.getInstance().getObjectFromResource(MiddlewareUserManagementServiceImplTest.class, fileName, aClass);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<AccountReferenceTO> getReferences(String... ibans) {
        return Arrays.stream(ibans)
                       .map(i -> getReference(i, EUR))
                       .collect(Collectors.toList());
    }

    private AccountReferenceTO getReference(String iban, Currency currency) {
        return new AccountReferenceTO(iban, null, null, null, null, currency);
    }

    private UserBO getUser(String branch) {
        UserBO user = new UserBO("test", "", "");
        user.setAccountAccesses(getAccesses());
        user.setBranch(branch);
        return user;
    }

    private List<AccountAccessBO> getAccesses() {
        return Arrays.asList(
                getAccessBO("1", EUR, 100),
                getAccessBO("1", USD, 50),
                getAccessBO("2", EUR, 60)
        );
    }

    private AccountAccessBO getAccessBO(String iban, Currency currency, int scaWeight) {
        AccountAccessBO access = new AccountAccessBO(iban, AccessTypeBO.OWNER);
        access.setCurrency(currency);
        access.setScaWeight(scaWeight);
        return access;
    }

    private AccountAccessTO getAccessTO() {
        return new AccountAccessTO("id", USER_IBAN, EUR, AccessTypeTO.OWNER, 100, ACCOUNT_ID);
    }

    private ScaInfoTO buildScaInfoTO() {
        return new ScaInfoTO(USER_ID, SCA_ID, AUTHORIZATION_ID, UserRoleTO.CUSTOMER, SCA_METHOD_ID, AUTH_CODE, TokenUsageTO.DELEGATED_ACCESS, USER_LOGIN);
    }

    private DepositAccountDetailsBO getDepositAccountDetailsBO() {
        return readYml(DepositAccountDetailsBO.class, "DepositAccountDetailsBO.yml");
    }

    private CustomPageableImpl getCustomPageableImpl() {
        CustomPageableImpl pageable = new CustomPageableImpl();
        pageable.setSize(10);
        return pageable;
    }

    private CustomPageImpl<Object> getCustomPageImpl() {
        CustomPageImpl<Object> page = new CustomPageImpl<>();
        page.setContent(Collections.singletonList(userTO));
        return page;
    }
}
