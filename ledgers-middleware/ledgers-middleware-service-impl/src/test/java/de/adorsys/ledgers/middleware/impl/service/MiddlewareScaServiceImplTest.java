package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.deposit.api.domain.AccountReferenceBO;
import de.adorsys.ledgers.deposit.api.domain.PaymentBO;
import de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO;
import de.adorsys.ledgers.deposit.api.service.DepositAccountPaymentService;
import de.adorsys.ledgers.middleware.api.domain.payment.TransactionStatusTO;
import de.adorsys.ledgers.middleware.api.domain.sca.*;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.impl.converter.BearerTokenMapperImpl;
import de.adorsys.ledgers.middleware.impl.converter.ScaInfoMapper;
import de.adorsys.ledgers.middleware.impl.converter.ScaResponseConverter;
import de.adorsys.ledgers.middleware.impl.converter.UserMapper;
import de.adorsys.ledgers.sca.domain.*;
import de.adorsys.ledgers.sca.service.SCAOperationService;
import de.adorsys.ledgers.sca.service.impl.SCAOperationServiceImpl;
import de.adorsys.ledgers.um.api.domain.*;
import de.adorsys.ledgers.um.api.service.AuthorizationService;
import de.adorsys.ledgers.um.api.service.UserService;
import de.adorsys.ledgers.util.exception.ScaModuleException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MiddlewareScaServiceImplTest {
    private static final String AUTH_ID = "V00authId";
    private static final String USER_ID = "sdfvgf7s96gfght";
    private static final String OPR_ID = "oprId";
    private static final LocalDateTime CREATED = LocalDateTime.now();
    private static final String METHOD_VALUE = "a@example.de";
    private static final String LOGIN = "anton.brueckner";
    private static final String PIN = "12345";
    private static final String METHOD_ID = "0001";
    private static final String IBAN = "DE1234567890";
    private static final String CODE = "123456";

    @InjectMocks
    private MiddlewareScaServiceImpl service;

    @Mock
    private UserService userService;
    @Mock
    private SCAOperationService scaOperationService;
    @Mock
    private ScaResponseConverter scaResponseConverter;
    @Mock
    private AuthorizationService authorizationService;
    @Mock
    private ScaResponseMessageResolver messageResolver;
    @Mock
    private DepositAccountPaymentService paymentService;
    @Mock
    private AccessService accessService;
    @Mock
    private ScaInfoMapper scaInfoMapper;

    private final ScaResponseConverter CONVERTER = new ScaResponseConverter(Mappers.getMapper(UserMapper.class), new ScaChallengeDataResolverImpl<>(Collections.emptyList()), new SCAOperationServiceImpl(null, null, null, null, null), new BearerTokenMapperImpl());

    @Test
    void loginForOperation_no_sca() {
        // Given
        when(userService.findByLogin(LOGIN))
                .thenReturn(getUserBO(false));

        SCAOperationBO scaOperation = getScaOperation(ScaStatusBO.RECEIVED, null);

        when(scaOperationService.checkIfExistsOrNew(any(AuthCodeDataBO.class)))
                .thenReturn(scaOperation);
        when(authorizationService.authorise(LOGIN, PIN, UserRoleBO.CUSTOMER, OPR_ID, AUTH_ID))
                .thenReturn(getBearer());
        when(authorizationService.scaToken(any())).thenReturn(getBearer());
        when(messageResolver.updateMessage(any(), any())).thenReturn("OK");
        when(scaResponseConverter.mapResponse(any(), anyList(), any(), any(), any(), anyInt(), any()))
                .thenAnswer(this::mapGlobalScaResponseTO);

        // When
        GlobalScaResponseTO result = service.loginForOperation(getLoginOprTO());

        // Then
        assertCommonFields(result);
        assertEquals(ScaStatusTO.EXEMPTED, result.getScaStatus());
        assertTrue(CollectionUtils.isEmpty(result.getScaMethods()));
        assertTrue(StringUtils.isNotBlank(result.getPsuMessage()));
        assertFalse(result.isMultilevelScaRequired());
        assertFalse(result.isPartiallyAuthorised());
        assertNotNull(result.getBearerToken());
    }

    @Test
    void loginForOperation_wrong_creds() {
        // Given
        when(userService.findByLogin(LOGIN))
                .thenReturn(getUserBO(false));

        SCAOperationBO scaOperation = getScaOperation(ScaStatusBO.RECEIVED, null);

        when(scaOperationService.checkIfExistsOrNew(any(AuthCodeDataBO.class)))
                .thenReturn(scaOperation);
        when(authorizationService.authorise(LOGIN, PIN, UserRoleBO.CUSTOMER, OPR_ID, AUTH_ID))
                .thenReturn(null);
        when(scaOperationService.updateFailedCount(anyString(), anyBoolean())).thenReturn(ScaModuleException.buildAttemptsException(1, true));

        // When
        assertThrows(ScaModuleException.class, () -> service.loginForOperation(getLoginOprTO()));
    }

    @Test
    void loginForOperation_sca_present() {
        // Given
        when(userService.findByLogin(LOGIN))
                .thenReturn(getUserBO(true));

        SCAOperationBO scaOperationBO = getScaOperation(ScaStatusBO.PSUIDENTIFIED, AuthCodeStatusBO.INITIATED);

        when(scaOperationService.checkIfExistsOrNew(any(AuthCodeDataBO.class)))
                .thenReturn(scaOperationBO);
        when(authorizationService.authorise(LOGIN, PIN, UserRoleBO.CUSTOMER, OPR_ID, AUTH_ID))
                .thenReturn(getBearer());
        when(authorizationService.scaToken(any())).thenReturn(getBearer());
        when(scaOperationService.createAuthCode(any(), any())).thenReturn(getScaOperation(ScaStatusBO.PSUIDENTIFIED, AuthCodeStatusBO.INITIATED));
        when(messageResolver.updateMessage(any(), any())).thenReturn("OK");
        when(scaResponseConverter.mapResponse(any(), anyList(), any(), any(), any(), anyInt(), any()))
                .thenAnswer(this::mapGlobalScaResponseTO);

        // When
        GlobalScaResponseTO result = service.loginForOperation(getLoginOprTO());

        // Then
        assertCommonFields(result);
        assertEquals(ScaStatusTO.PSUIDENTIFIED, result.getScaStatus());
        assertEquals(1, result.getScaMethods().size());
        assertTrue(StringUtils.isNotBlank(result.getPsuMessage()));
        assertFalse(result.isMultilevelScaRequired());
        assertFalse(result.isPartiallyAuthorised());
        assertNotNull(result.getBearerToken());
    }

    @Test
    void getMethods() {
        // Given
        when(userService.findById(USER_ID))
                .thenReturn(getUserBO(true));
        SCAOperationBO scaOperationBO = getScaOperation(ScaStatusBO.PSUIDENTIFIED, AuthCodeStatusBO.INITIATED);
        when(scaOperationService.loadAuthCode(AUTH_ID))
                .thenReturn(scaOperationBO);
        when(messageResolver.updateMessage(any(), any())).thenReturn("OK");
        when(scaResponseConverter.mapResponse(any(), any(), any(), anyString(), any(), anyInt(), any()))
                .thenAnswer(this::mapGlobalScaResponseTO);
        // When
        GlobalScaResponseTO result = service.getMethods(AUTH_ID, USER_ID);

        // Then
        assertCommonFields(result);
        assertEquals(ScaStatusTO.PSUIDENTIFIED, result.getScaStatus());
        assertEquals(1, result.getScaMethods().size());
        assertTrue(StringUtils.isNotBlank(result.getPsuMessage()));
        assertFalse(result.isMultilevelScaRequired());
        assertFalse(result.isPartiallyAuthorised());
        assertNull(result.getBearerToken());
    }

    @Test
    void selectMethod() {
        when(scaOperationService.loadAuthCode(anyString())).thenReturn(getScaOperation(ScaStatusBO.SCAMETHODSELECTED, AuthCodeStatusBO.INITIATED));
        when(userService.findById(anyString())).thenReturn(getUserBO(true));
        when(paymentService.getPaymentById(any())).thenReturn(getMockPayment());
        when(accessService.resolveScaWeightCommon(anySet(), anyList())).thenReturn(100);
        when(messageResolver.getTemplate(any())).thenReturn("Message");
        when(scaOperationService.generateAuthCode(any(), any(), any())).thenReturn(getScaOperation(ScaStatusBO.SCAMETHODSELECTED, AuthCodeStatusBO.SENT));
        when(messageResolver.updateMessage(any(), any())).thenReturn("OK");
        when(scaResponseConverter.mapResponse(any(), anyList(), any(), anyString(), any(), anyInt(), any())).thenAnswer(this::mapGlobalScaResponseTO);

        ScaInfoTO info = new ScaInfoTO(USER_ID, null, AUTH_ID, UserRoleTO.CUSTOMER, METHOD_ID, null, null, "");
        GlobalScaResponseTO result = service.selectMethod(info);

        assertCommonFields(result);
        assertEquals(ScaStatusTO.SCAMETHODSELECTED, result.getScaStatus());
        assertEquals(1, result.getScaMethods().size());
        assertTrue(StringUtils.isNotBlank(result.getPsuMessage()));
        assertFalse(result.isMultilevelScaRequired());
        assertFalse(result.isPartiallyAuthorised());
    }

    @Test
    void confirmAuth() {
        // Given
        when(scaOperationService.loadAuthCode(anyString())).thenReturn(getScaOperation(ScaStatusBO.SCAMETHODSELECTED, AuthCodeStatusBO.SENT));
        when(paymentService.getPaymentById(any())).thenReturn(getMockPayment());
        when(userService.findById(anyString())).thenReturn(getUserBO(true));
        when(messageResolver.getTemplate(any())).thenReturn("Message");
        when(scaOperationService.validateAuthCode(anyString(), anyString(), anyString(), anyInt())).thenReturn(new ScaValidationBO(CODE, true, ScaStatusBO.FINALISED, 3));
        when(scaOperationService.authenticationCompleted(anyString(), any())).thenReturn(true);
        when(paymentService.executePayment(anyString(), anyString())).thenReturn(TransactionStatusBO.ACSC);
        when(scaInfoMapper.toScaInfoBO(any())).thenReturn(new ScaInfoBO(USER_ID, null, AUTH_ID, UserRoleBO.CUSTOMER, METHOD_ID, CODE, null, LOGIN));
        when(accessService.resolveScaWeightCommon(anySet(), anyList())).thenReturn(100);
        when(messageResolver.updateMessage(any(), any())).thenReturn("OK");
        when(scaResponseConverter.mapResponse(any(), anyList(), any(), anyString(), any(), anyInt(), any())).thenAnswer(this::mapGlobalScaResponseTO);

        ScaInfoTO info = new ScaInfoTO(USER_ID, null, AUTH_ID, UserRoleTO.CUSTOMER, METHOD_ID, CODE, null, LOGIN);

        // When
        GlobalScaResponseTO result = service.confirmAuthorization(info);

        // Then
        assertCommonFields(result);
        assertEquals(ScaStatusTO.FINALISED, result.getScaStatus());
        assertEquals(1, result.getScaMethods().size());
        assertTrue(StringUtils.isNotBlank(result.getPsuMessage()));
        assertFalse(result.isMultilevelScaRequired());
        assertFalse(result.isPartiallyAuthorised());
        assertEquals(TransactionStatusTO.ACSC, result.getTransactionStatus());
    }

    @Test
    void confirmAuth_authNotCompleted_multilevelSca() throws NoSuchFieldException {
        // Given
        FieldSetter.setField(service, service.getClass().getDeclaredField("multilevelScaEnable"), true);

        when(scaOperationService.loadAuthCode(anyString())).thenReturn(getScaOperation(ScaStatusBO.SCAMETHODSELECTED, AuthCodeStatusBO.SENT));
        when(paymentService.getPaymentById(any())).thenReturn(getMockPayment());
        when(userService.findById(anyString())).thenReturn(getUserBO(true));
        when(messageResolver.getTemplate(any())).thenReturn("Message");
        when(scaOperationService.validateAuthCode(anyString(), anyString(), anyString(), anyInt())).thenReturn(new ScaValidationBO(CODE, true, ScaStatusBO.FINALISED, 3));
        when(scaOperationService.authenticationCompleted(anyString(), any())).thenReturn(false);
        when(scaInfoMapper.toScaInfoBO(any())).thenReturn(new ScaInfoBO(USER_ID, null, AUTH_ID, UserRoleBO.CUSTOMER, METHOD_ID, CODE, null, LOGIN));
        when(accessService.resolveScaWeightCommon(anySet(), anyList())).thenReturn(100);
        when(messageResolver.updateMessage(any(), any())).thenReturn("OK");
        when(scaResponseConverter.mapResponse(any(), anyList(), any(), anyString(), any(), anyInt(), any())).thenAnswer(this::mapGlobalScaResponseTO);
        when(paymentService.updatePaymentStatus(any(), any())).thenReturn(TransactionStatusBO.PATC);

        ScaInfoTO info = new ScaInfoTO(USER_ID, null, AUTH_ID, UserRoleTO.CUSTOMER, METHOD_ID, CODE, null, LOGIN);

        // When
        GlobalScaResponseTO result = service.confirmAuthorization(info);

        // Then
        assertCommonFields(result);
        assertEquals(ScaStatusTO.FINALISED, result.getScaStatus());
        assertEquals(1, result.getScaMethods().size());
        assertTrue(StringUtils.isNotBlank(result.getPsuMessage()));
        assertFalse(result.isMultilevelScaRequired());
        assertFalse(result.isPartiallyAuthorised());
        assertEquals(TransactionStatusTO.PATC, result.getTransactionStatus());
    }

    @Test
    void confirmAuth_authNotCompleted_notMultilevelSca() throws NoSuchFieldException {
        // Given
        FieldSetter.setField(service, service.getClass().getDeclaredField("multilevelScaEnable"), false);

        when(scaOperationService.loadAuthCode(anyString())).thenReturn(getScaOperation(ScaStatusBO.SCAMETHODSELECTED, AuthCodeStatusBO.SENT));
        when(paymentService.getPaymentById(any())).thenReturn(getMockPayment());
        when(userService.findById(anyString())).thenReturn(getUserBO(true));
        when(messageResolver.getTemplate(any())).thenReturn("Message");
        when(scaOperationService.validateAuthCode(anyString(), anyString(), anyString(), anyInt())).thenReturn(new ScaValidationBO(CODE, true, ScaStatusBO.FINALISED, 3));
        when(scaOperationService.authenticationCompleted(anyString(), any())).thenReturn(false);
        when(scaInfoMapper.toScaInfoBO(any())).thenReturn(new ScaInfoBO(USER_ID, null, AUTH_ID, UserRoleBO.CUSTOMER, METHOD_ID, CODE, null, LOGIN));
        when(accessService.resolveScaWeightCommon(anySet(), anyList())).thenReturn(100);
        when(messageResolver.updateMessage(any(), any())).thenReturn("OK");
        when(scaResponseConverter.mapResponse(any(), anyList(), any(), anyString(), any(), anyInt(), any())).thenAnswer(this::mapGlobalScaResponseTO);
        PaymentBO payment = getMockPayment();
        payment.setTransactionStatus(TransactionStatusBO.RCVD);
        when(paymentService.getPaymentById(any()))
                .thenReturn(payment);

        ScaInfoTO info = new ScaInfoTO(USER_ID, null, AUTH_ID, UserRoleTO.CUSTOMER, METHOD_ID, CODE, null, LOGIN);

        // When
        GlobalScaResponseTO result = service.confirmAuthorization(info);

        // Then
        assertCommonFields(result);
        assertEquals(ScaStatusTO.FINALISED, result.getScaStatus());
        assertEquals(1, result.getScaMethods().size());
        assertTrue(StringUtils.isNotBlank(result.getPsuMessage()));
        assertFalse(result.isMultilevelScaRequired());
        assertFalse(result.isPartiallyAuthorised());
        assertEquals(TransactionStatusTO.RCVD, result.getTransactionStatus());
    }

    private void assertCommonFields(GlobalScaResponseTO result) {
        assertNotNull(result);
        assertEquals(OpTypeTO.PAYMENT, result.getOpType());
        assertEquals(OPR_ID, result.getOperationObjectId());
        assertEquals(AUTH_ID, result.getAuthorisationId());
    }

    private PaymentBO getMockPayment() {
        PaymentBO bo = new PaymentBO();
        AccountReferenceBO ref = new AccountReferenceBO();
        ref.setIban(IBAN);
        bo.setDebtorAccount(ref);
        return bo;
    }

    private GlobalScaResponseTO mapGlobalScaResponseTO(InvocationOnMock a) {
        return CONVERTER.mapResponse(a.getArgument(0), a.getArgument(1), a.getArgument(2), a.getArgument(3), a.getArgument(4), a.getArgument(5), a.getArgument(6));
    }

    private SCAOperationBO getScaOperation(ScaStatusBO scaStatus, AuthCodeStatusBO authStatus) {
        SCAOperationBO bo = new SCAOperationBO();
        bo.setCreated(CREATED);
        bo.setStatusTime(CREATED);
        bo.setFailledCount(0);
        bo.setId(AUTH_ID);
        bo.setOpId(OPR_ID);
        bo.setOpType(OpTypeBO.PAYMENT);
        bo.setValiditySeconds(1000);
        bo.setStatus(authStatus);
        bo.setScaStatus(scaStatus);
        return bo;
    }

    private BearerTokenBO getBearer() {
        BearerTokenBO bo = new BearerTokenBO();
        bo.setAccess_token("token");
        bo.setAccessTokenObject(new AccessTokenBO());
        bo.setExpires_in(1000);
        bo.setToken_type("type");
        return bo;
    }

    private UserBO getUserBO(boolean scaPresent) {
        UserBO bo = new UserBO();
        bo.setId(USER_ID);
        bo.setPin(PIN);
        bo.setLogin(LOGIN);
        bo.setAccountAccesses(getAccess());
        bo.setScaUserData(scaPresent ? getScaUserData() : Collections.emptyList());
        bo.setUserRoles(Collections.singletonList(UserRoleBO.CUSTOMER));
        return bo;
    }

    private List<ScaUserDataBO> getScaUserData() {
        ScaUserDataBO bo = new ScaUserDataBO();
        bo.setId("methodId");
        bo.setMethodValue(METHOD_VALUE);
        bo.setScaMethod(ScaMethodTypeBO.EMAIL);
        bo.setValid(true);
        return Collections.singletonList(bo);
    }

    private List<AccountAccessBO> getAccess() {
        AccountAccessBO bo = new AccountAccessBO();
        bo.setAccountId("accId");
        bo.setAccessType(AccessTypeBO.OWNER);
        bo.setIban("iban");
        bo.setCurrency(Currency.getInstance("EUR"));
        return Collections.singletonList(bo);
    }

    private ScaLoginOprTO getLoginOprTO() {
        ScaLoginOprTO login = new ScaLoginOprTO();
        login.setLogin(LOGIN);
        login.setPin(PIN);
        login.setAuthorisationId(AUTH_ID);
        login.setOprId(OPR_ID);
        login.setOpType(OpTypeTO.LOGIN);
        return login;
    }
}