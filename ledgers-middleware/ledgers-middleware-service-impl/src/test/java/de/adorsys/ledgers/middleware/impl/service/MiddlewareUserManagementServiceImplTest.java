package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.deposit.api.domain.AccountStatusBO;
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
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.PageImpl;
import pro.javatar.commons.reader.YamlReader;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MiddlewareUserManagementServiceImplTest {
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

    @BeforeClass
    public static void before() {
        userBO = readYml(UserBO.class, "user.yml");
        userTO = readYml(UserTO.class, "user.yml");
    }

    @Test
    public void create() {
        //given
        when(userService.create(any())).thenReturn(userBO);

        //when
        UserTO user = middlewareUserService.create(userTO);

        //then
        assertThat(user).isNotNull();
        assertThat(user).isEqualToComparingFieldByField(userTO);
        verify(userService, times(1)).create(userBO);
    }

    @Test
    public void findById() {
        //given
        when(userService.findById(any())).thenReturn(userBO);

        //when
        UserTO user = middlewareUserService.findById(USER_ID);

        //then
        assertThat(user).isNotNull();
        assertThat(user).isEqualToComparingFieldByField(userTO);
        verify(userService, times(1)).findById(USER_ID);
    }

    @Test(expected = UserManagementModuleException.class)
    public void findByUserLoginUserNotFound() {
        //given
        String login = "spe@adorsys.com.ua";
        when(userService.findByLogin(login)).thenThrow(UserManagementModuleException.builder().build());

        //when
        middlewareUserService.findByUserLogin(login);
    }

    @Test
    public void updateScaMethods() {
        //given
        String userLogin = "userLogin";
        when(userService.updateScaData(userBO.getScaUserData(), userLogin)).thenReturn(userBO);

        //when
        middlewareUserService.updateScaData(userLogin, userTO.getScaUserData());

        //then
        verify(userService, times(1)).updateScaData(userBO.getScaUserData(), userLogin);
    }

    @Test
    public void updateAccountAccess() {
        //given
        when(depositAccountService.getAccountDetailsByIbanAndCurrency(any(), any(), any(), anyBoolean())).thenReturn(getDepositAccountDetailsBO());
        when(userService.findById(any())).thenReturn(userBO);
        when(accessService.userHasAccessToAccount(any(), any())).thenReturn(true);

        //when
        middlewareUserService.updateAccountAccess(buildScaInfoTO(), USER_ID, getAccessTO());

        //then
        verify(userService, times(2)).findById(USER_ID);
    }

    @Test(expected = MiddlewareModuleException.class)
    public void updateAccountAccess_wrong_branch_access() {
        //given
        when(depositAccountService.getAccountDetailsByIbanAndCurrency(any(), any(), any(), anyBoolean())).thenReturn(getDepositAccountDetailsBO());
        when(userService.findById(ANOTHER_USER_ID)).thenReturn(getUser(ANOTHER_USER_ID));
        when(userService.findById(USER_ID)).thenReturn(userBO);
        when(accessService.userHasAccessToAccount(any(), any())).thenReturn(true);

        //when
        middlewareUserService.updateAccountAccess(buildScaInfoTO(), ANOTHER_USER_ID, getAccessTO());

        //then
        verify(userService, times(2)).findById(USER_ID);
    }

    @Test(expected = MiddlewareModuleException.class)
    public void updateAccountAccess_accountNotEnabled() {
        //given
        DepositAccountDetailsBO accountDetails = getDepositAccountDetailsBO();
        accountDetails.getAccount().setAccountStatus(AccountStatusBO.BLOCKED);
        when(depositAccountService.getAccountDetailsByIbanAndCurrency(any(), any(), any(), anyBoolean())).thenReturn(accountDetails);

        //when
        middlewareUserService.updateAccountAccess(buildScaInfoTO(), USER_ID, getAccessTO());
    }

    @Test(expected = MiddlewareModuleException.class)
    public void updateAccountAccess_noAccessToAccount() {
        //given
        when(depositAccountService.getAccountDetailsByIbanAndCurrency(any(), any(), any(), anyBoolean())).thenReturn(getDepositAccountDetailsBO());
        when(userService.findById(any())).thenReturn(userBO);
        when(accessService.userHasAccessToAccount(any(), any())).thenReturn(false);

        //when
        middlewareUserService.updateAccountAccess(buildScaInfoTO(), USER_ID, getAccessTO());
    }

    @Test
    public void listUsers() {
        //given
        when(userService.listUsers(anyInt(), anyInt())).thenReturn(Collections.singletonList(userBO));

        //when
        middlewareUserService.listUsers(0, 15);

        //then
        verify(userService, times(1)).listUsers(0, 15);
    }

    @Test
    public void getUsersByBranchAndRoles() {
        //given
        when(userService.findByBranchAndUserRolesIn(any(), any(), any(), any())).thenReturn(new PageImpl<UserBO>(Collections.singletonList(userBO)));
        when(pageMapper.toCustomPageImpl(any())).thenReturn(getCustomPageImpl());

        //when
        CustomPageImpl<UserTO> users = middlewareUserService.getUsersByBranchAndRoles(USER_BRANCH, Collections.singletonList(UserRoleTO.CUSTOMER), "", getCustomPageableImpl());

        //then
        assertThat(users.getContent().get(0)).isNotNull();
        assertThat(users.getContent().get(0)).isEqualToComparingFieldByField(userTO);
    }

    @Test
    public void countUsersByBranch() {
        //given
        when(userService.countUsersByBranch(any())).thenReturn(2);

        //when
        int result = middlewareUserService.countUsersByBranch(USER_BRANCH);

        //then
        assertEquals(2, result);
        verify(userService, times(1)).countUsersByBranch(USER_BRANCH);
    }

    @Test
    public void updateUser() {
        //given
        UserTO user = userTO;
        user.setId(USER_ID);
        when(userService.findById(any())).thenReturn(userBO);
        when(userService.updateUser(any())).thenReturn(userBO);

        //when
        UserTO result = middlewareUserService.updateUser(BRANCH_ID, user);

        //then
        assertThat(result).isNotNull();
        verify(userService, times(1)).findById(USER_ID);
    }

    @Test(expected = MiddlewareModuleException.class)
    public void updateUser_userIdNull() {
        //when
        middlewareUserService.updateUser(BRANCH_ID, userTO);
    }

    @Test(expected = MiddlewareModuleException.class)
    public void updateUser_branchesNotMatch() {
        //given
        UserTO user = userTO;
        user.setId(USER_ID);
        when(userService.findById(any())).thenReturn(userBO);

        //when
        middlewareUserService.updateUser("anotherBranch", user);
    }

    @Test
    public void getSCAMethods() {
        //given
        String userLogin = "spe@adorsys.com.ua";
        when(userService.findByLogin(userLogin)).thenReturn(userBO);

        //when
        UserTO user = middlewareUserService.findByUserLogin(userLogin);

        //then
        assertThat(user.getScaUserData().size()).isEqualTo((2));
        assertThat(user.getScaUserData().get(0).getScaMethod()).isEqualByComparingTo(ScaMethodTypeTO.EMAIL);
        assertThat(user.getScaUserData().get(0).getMethodValue()).isEqualTo("spe@adorsys.com.ua");
        assertThat(user.getScaUserData().get(1).getScaMethod()).isEqualTo(ScaMethodTypeTO.MOBILE);
        assertThat(user.getScaUserData().get(1).getMethodValue()).isEqualTo("+380933686868");
        verify(userService, times(1)).findByLogin(userLogin);
    }

    @Test
    public void checkMultilevelScaRequired_multilevelNotEnabled() {
        //given
        Whitebox.setInternalState(middlewareUserService, "multilevelScaEnable", false);

        //when
        boolean result = middlewareUserService.checkMultilevelScaRequired(USER_LOGIN, USER_IBAN);

        //then
        assertThat(result).isFalse();
    }

    @Test(expected = MiddlewareModuleException.class)
    public void checkMultilevelScaRequired_noAccess() {
        //given
        Whitebox.setInternalState(middlewareUserService, "multilevelScaEnable", true);
        when(userService.findByLogin(any())).thenReturn(userBO);

        //when
        middlewareUserService.checkMultilevelScaRequired(USER_LOGIN, "");
    }

    @Test
    public void checkMultilevelScaRequired() {
        //given
        Whitebox.setInternalState(middlewareUserService, "multilevelScaEnable", true);
        when(userService.findByLogin(any())).thenReturn(userBO);
        when(accessService.resolveScaWeightByDebtorAccount(any(), any())).thenReturn(100);

        //when
        boolean result = middlewareUserService.checkMultilevelScaRequired(USER_LOGIN, "1234567");

        //then
        assertThat(result).isFalse();
    }

    @Test
    public void checkMultilevelScaRequired_no_multilevel() {
        //given
        Whitebox.setInternalState(middlewareUserService, "multilevelScaEnable", true);
        when(userService.findByLogin(any())).thenReturn(getUser(null));

        //when
        boolean response = middlewareUserService.checkMultilevelScaRequired("some_login", getReferences("1"));

        //then
        assertThat(response).isFalse();
    }

    @Test
    public void checkMultilevelScaRequired_empty_list() {
        //given
        Whitebox.setInternalState(middlewareUserService, "multilevelScaEnable", true);
        when(userService.findByLogin(any())).thenReturn(getUser(null));

        //when
        boolean response = middlewareUserService.checkMultilevelScaRequired("some_login", new ArrayList<>());

        //then
        assertThat(response).isTrue();
    }

    @Test
    public void checkMultilevelScaRequired_2_acc_with_mlsca() {
        //given
        Whitebox.setInternalState(middlewareUserService, "multilevelScaEnable", true);
        when(userService.findByLogin(any())).thenReturn(getUser(null));

        //when
        boolean response = middlewareUserService.checkMultilevelScaRequired("some_login", getReferences("1", "2"));

        //then
        assertThat(response).isTrue();
    }

    @Test
    public void checkMultilevelScaRequired_1_acc_no_curr_with_mlsca() {
        //given
        Whitebox.setInternalState(middlewareUserService, "multilevelScaEnable", true);
        when(userService.findByLogin(any())).thenReturn(getUser(null));

        //when
        boolean response = middlewareUserService.checkMultilevelScaRequired("some_login", Collections.singletonList(getReference("1", null)));

        //then
        assertThat(response).isTrue();
    }

    @Test(expected = MiddlewareModuleException.class)
    public void checkMultilevelScaRequired_acc_not_match() {
        //given
        Whitebox.setInternalState(middlewareUserService, "multilevelScaEnable", true);
        when(userService.findByLogin(any())).thenReturn(getUser(null));

        //when
        middlewareUserService.checkMultilevelScaRequired("some_login", getReferences("1", "3"));
    }

    @Test
    public void checkMultilevelScaRequired_multilevel_false() {
        //given
        Whitebox.setInternalState(middlewareUserService, "multilevelScaEnable", false);

        //when
        boolean response = middlewareUserService.checkMultilevelScaRequired("some_login", getReferences("1", "2"));

        //then
        assertThat(response).isFalse();
    }

    @Test
    public void getAdditionalInformation(){
        Whitebox.setInternalState(middlewareUserService, "additionalInfoMapper", Mappers.getMapper(AdditionalAccountInformationMapper.class));
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
