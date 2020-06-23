package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.deposit.api.domain.*;
import de.adorsys.ledgers.deposit.api.service.DepositAccountPaymentService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.middleware.api.domain.account.AccountReferenceTO;
import de.adorsys.ledgers.middleware.api.domain.payment.*;
import de.adorsys.ledgers.middleware.api.domain.sca.SCAPaymentResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.um.AccessTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareModuleException;
import de.adorsys.ledgers.middleware.impl.config.PaymentProductsConfig;
import de.adorsys.ledgers.middleware.impl.converter.*;
import de.adorsys.ledgers.middleware.impl.policies.PaymentCoreDataPolicy;
import de.adorsys.ledgers.middleware.impl.policies.PaymentCoreDataPolicyHelper;
import de.adorsys.ledgers.middleware.impl.sca.EmailScaChallengeData;
import de.adorsys.ledgers.sca.domain.OpTypeBO;
import de.adorsys.ledgers.sca.domain.SCAOperationBO;
import de.adorsys.ledgers.sca.domain.ScaStatusBO;
import de.adorsys.ledgers.sca.domain.ScaValidationBO;
import de.adorsys.ledgers.sca.service.SCAOperationService;
import de.adorsys.ledgers.um.api.domain.BearerTokenBO;
import de.adorsys.ledgers.um.api.domain.ScaInfoBO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.service.AuthorizationService;
import de.adorsys.ledgers.um.api.service.UserService;
import de.adorsys.ledgers.util.exception.DepositModuleException;
import de.adorsys.ledgers.util.exception.ScaModuleException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.javatar.commons.reader.YamlReader;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO.ACSP;
import static de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO.ACTC;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MiddlewarePaymentServiceImplTest {
    private static final String PAYMENT_ID = "myPaymentId";
    private static final String SINGLE_BO = "PaymentSingle.yml";
    private static final String SINGLE_TO = "PaymentSingleTO.yml";
    private static final String WRONG_PAYMENT_ID = "wrong id";
    private static final String USER_ID = "kjk345knkj45";
    private static final String SCA_ID = "scaId";
    private static final String SCA_METHOD_ID = "scaMethodId";
    private static final String AUTH_CODE = "123456";
    private static final String AUTHORISATION_ID = "authorisationId";
    private static final String USER_LOGIN = "userLogin";
    private static final String IBAN = "DE1234567890";
    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Currency USD = Currency.getInstance("USD");

    @InjectMocks
    private MiddlewarePaymentServiceImpl middlewareService;

    @Mock
    private DepositAccountPaymentService paymentService;
    @Mock
    private SCAOperationService operationService;
    @Mock
    private PaymentConverter paymentConverter;
    @Mock
    private DepositAccountService accountService;
    @Mock
    private AuthCodeDataConverter codeDataConverter;
    @Mock
    private MiddlewareUserManagementServiceImpl userManagementService;
    @Mock
    private AccessTokenTO accessToken;
    @Mock
    private SCAUtils scaUtils;
    @Mock
    private UserService userService;
    @Mock
    private BearerTokenMapper bearerTokenMapper;
    @Mock
    private PaymentCoreDataPolicy coreDataPolicy;
    @Mock
    private AccessTokenMapper accessTokenMapper;
    @Mock
    private AccessService accessService;
    @Mock
    private ScaInfoMapper scaInfoMapper;
    @Mock
    private AuthorizationService authorizationService;
    @Mock
    private ScaResponseResolver scaResponseResolver;
    @Mock
    private AccountDetailsMapper detailsMapper;
    @Mock
    private PaymentProductsConfig paymentProductsConfig;

    private ScaResponseResolver localResolver = new ScaResponseResolver(scaUtils, new ScaChallengeDataResolverImpl(Arrays.asList(new EmailScaChallengeData())), Mappers.getMapper(UserMapper.class), operationService);

    private final PaymentConverter pmtMapper = Mappers.getMapper(PaymentConverter.class);

    @Test
    void getPaymentStatusById() {
        // Given
        when(paymentService.getPaymentStatusById(PAYMENT_ID)).thenReturn(TransactionStatusBO.RJCT);

        // When
        TransactionStatusTO paymentResult = middlewareService.getPaymentStatusById(PAYMENT_ID);

        // Then
        assertEquals(TransactionStatusBO.RJCT.getName(), paymentResult.getName());
        verify(paymentService, times(1)).getPaymentStatusById(PAYMENT_ID);
    }

    @Test
    void getPaymentStatusByIdWithException() {
        // Given
        when(paymentService.getPaymentStatusById(PAYMENT_ID)).thenThrow(DepositModuleException.class);

        // Then
        assertThrows(DepositModuleException.class, () -> middlewareService.getPaymentStatusById(PAYMENT_ID));
        verify(paymentService, times(1)).getPaymentStatusById(PAYMENT_ID);
    }

    @Test
    void generateAuthCode() {
        // Given
        UserBO userBO = readYml(UserBO.class, "user1.yml");
        String paymentId = "myPaymentId";
        PaymentBO payment = readYml(PaymentBO.class, SINGLE_BO);

        when(scaUtils.userBO(USER_ID)).thenReturn(userBO);
        when(paymentService.getPaymentById(paymentId)).thenReturn(payment);
        when(coreDataPolicy.getPaymentCoreData(any(), eq(payment))).thenReturn(PaymentCoreDataPolicyHelper.getPaymentCoreDataInternal(payment));
        when(scaResponseResolver.updatePaymentRelatedResponseFields(any(), any())).thenAnswer(i -> localResolver.updatePaymentRelatedResponseFields((SCAPaymentResponseTO) i.getArguments()[0], (PaymentBO) i.getArguments()[1]));

        // When
        SCAPaymentResponseTO responseTO = middlewareService.selectSCAMethodForPayment(buildScaInfoTO(), paymentId);

        // Then
        assertEquals(paymentId, responseTO.getPaymentId());
    }

    @Test
    void getPaymentById() {
        // Given
        when(paymentService.getPaymentById(PAYMENT_ID)).thenReturn(readYml(PaymentBO.class, SINGLE_BO));
        when(paymentConverter.toPaymentTO(any())).thenReturn(readYml(PaymentTO.class, SINGLE_BO));

        // When
        PaymentTO result = middlewareService.getPaymentById(PAYMENT_ID);

        // Then
        assertNotNull(result);
    }

    @Test
    void getPaymentById_Fail_wrong_id() {
        // Given
        when(paymentService.getPaymentById(WRONG_PAYMENT_ID)).thenThrow(DepositModuleException.class);

        // Then
        assertThrows(DepositModuleException.class, () -> middlewareService.getPaymentById(WRONG_PAYMENT_ID));
    }

    @Test
    void execute_Payment_For_Any_Type_Of_Sca_Users() {
        // Given
        UserBO userBO = readYml(UserBO.class, "user1.yml");
        PaymentBO paymentBO = readYml(PaymentBO.class, SINGLE_BO);

        when(paymentConverter.toPaymentBO(any(PaymentTO.class))).thenReturn(paymentBO);
        List<DepositAccountBO> accounts = Collections.singletonList(DepositAccountBO.builder()
                                                                            .iban(paymentBO.getDebtorAccount().getIban())
                                                                            .currency(paymentBO.getDebtorAccount().getCurrency())
                                                                            .build());
        when(accountService.getAccountsByIbanAndParamCurrency(any(), any())).thenReturn(accounts);
        when(paymentService.initiatePayment(any(), any())).thenReturn(paymentBO);
        when(paymentService.executePayment(any(), any())).thenReturn(ACTC);
        when(authorizationService.scaToken(any(ScaInfoBO.class))).thenReturn(new BearerTokenBO());
        when(bearerTokenMapper.toBearerTokenTO(any())).thenReturn(new BearerTokenTO());
        when(scaUtils.userBO(anyString())).thenReturn(userBO);

        // When
        SCAPaymentResponseTO response = middlewareService.executePayment(buildScaInfoTO(), readYml(PaymentTO.class, SINGLE_BO));

        // Then
        assertNotNull(response);
        assertEquals(TransactionStatusTO.ACTC, response.getTransactionStatus());
    }

    @Test
    void initiatePayment() throws NoSuchFieldException {
        // Given
        UserBO userBO = readYml(UserBO.class, "user1.yml");
        PaymentBO paymentBO = readYml(PaymentBO.class, SINGLE_BO);
        when(coreDataPolicy.getPaymentCoreData(any(), eq(paymentBO))).thenReturn(PaymentCoreDataPolicyHelper.getPaymentCoreDataInternal(paymentBO));

        when(paymentConverter.toPaymentBO(any(PaymentTO.class), any())).thenReturn(paymentBO);
        when(accountService.getAccountsByIbanAndParamCurrency(any(), any())).thenReturn(Collections.singletonList(new DepositAccountBO("", paymentBO.getDebtorAccount().getIban(), null, null, null, null, paymentBO.getDebtorAccount().getCurrency(), null, null, null, null, null, null, null, false, false, "branch", null)));
        when(paymentService.initiatePayment(any(), any())).thenReturn(paymentBO);
        when(paymentService.executePayment(any(), any())).thenReturn(TransactionStatusBO.ACSP);
        when(bearerTokenMapper.toBearerTokenTO(any())).thenReturn(new BearerTokenTO());
        when(scaUtils.userBO(USER_ID)).thenReturn(userBO);
        when(scaResponseResolver.updatePaymentRelatedResponseFields(any(), any())).thenAnswer(i -> localResolver.updatePaymentRelatedResponseFields((SCAPaymentResponseTO) i.getArguments()[0], (PaymentBO) i.getArguments()[1]));

        FieldSetter.setField(middlewareService, middlewareService.getClass().getDeclaredField("paymentProductsConfig"), getPaymentConfig());

        // When
        Object result = middlewareService.initiatePayment(buildScaInfoTO(), readYml(PaymentTO.class, SINGLE_BO), PaymentTypeTO.SINGLE);

        // Then
        assertNotNull(result);
    }

    @Test
    void initiatePayment_creditor_account_disabled() throws NoSuchFieldException {
        // Given
        PaymentBO paymentBO = readYml(PaymentBO.class, SINGLE_BO);

        when(paymentConverter.toPaymentBO(any(PaymentTO.class), any())).thenReturn(paymentBO);
        when(accountService.getAccountsByIbanAndParamCurrency(any(), any())).thenReturn(Collections.singletonList(new DepositAccountBO("", paymentBO.getDebtorAccount().getIban(), null, null, null, null, paymentBO.getDebtorAccount().getCurrency(), null, null, null, null, null, null, null, false, false, null, null)));
        when(accountService.getAccountsByIbanAndParamCurrency(eq("DE91100000000123456709"), any())).thenReturn(Collections.singletonList(new DepositAccountBO("", paymentBO.getTargets().iterator().next().getCreditorAccount().getIban(), null, null, null, null, paymentBO.getDebtorAccount().getCurrency(), null, null, null, null, null, null, null, true, false, null, null)));

        FieldSetter.setField(middlewareService, middlewareService.getClass().getDeclaredField("paymentProductsConfig"), getPaymentConfig());

        // Then
        assertThrows(MiddlewareModuleException.class, () -> {
            Object result = middlewareService.initiatePayment(buildScaInfoTO(), readYml(PaymentTO.class, SINGLE_BO), PaymentTypeTO.SINGLE);
            assertNotNull(result);
        });
    }

    @Test
    void initiatePayment_invalid_amount() throws NoSuchFieldException {
        // Given
        PaymentBO paymentBO = readYml(PaymentBO.class, SINGLE_BO);
        paymentBO.getTargets().iterator().next().getInstructedAmount().setAmount(BigDecimal.ZERO);

        when(paymentConverter.toPaymentBO(any(PaymentTO.class), any())).thenReturn(paymentBO);

        FieldSetter.setField(middlewareService, middlewareService.getClass().getDeclaredField("paymentProductsConfig"), getPaymentConfig());

        // Then
        assertThrows(MiddlewareModuleException.class, () -> {
            Object result = middlewareService.initiatePayment(buildScaInfoTO(), readYml(PaymentTO.class, SINGLE_BO), PaymentTypeTO.SINGLE);
            assertNotNull(result);

        });
    }

    @Test
    void initiatePayment_unsupported_product() throws NoSuchFieldException {
        // Given
        PaymentBO paymentBO = readYml(PaymentBO.class, SINGLE_BO);
        paymentBO.setPaymentProduct("zzz");

        when(paymentConverter.toPaymentBO(any(PaymentTO.class), any())).thenReturn(paymentBO);

        FieldSetter.setField(middlewareService, middlewareService.getClass().getDeclaredField("paymentProductsConfig"), getPaymentConfig());

        // Then
        assertThrows(MiddlewareModuleException.class, () -> {
            Object result = middlewareService.initiatePayment(buildScaInfoTO(), readYml(PaymentTO.class, SINGLE_BO), PaymentTypeTO.SINGLE);
            assertNotNull(result);
        });
    }

    @Test
    void initPmtRejectByCurrencySuccess() throws NoSuchFieldException {
        // Given
        //Payment: debtor - EUR / amount - EUR // Account - EUR
        PaymentTO paymentTO = getPayment(EUR, EUR);
        PaymentBO paymentBO = pmtMapper.toPaymentBO(paymentTO);
        paymentBO.setTransactionStatus(TransactionStatusBO.ACSC);
        paymentBO.setPaymentProduct("instant-sepa-credit-transfers");
        UserBO userBO = new UserBO("Test", "", "");

        when(accountService.getAccountsByIbanAndParamCurrency(any(), any())).thenReturn(getAccounts(false, EUR));

        when(paymentConverter.toPaymentBO(any(PaymentTO.class), any())).thenReturn(paymentBO);
        when(scaUtils.userBO(USER_ID)).thenReturn(userBO);
        when(paymentService.initiatePayment(any(), any())).thenReturn(paymentBO);
        when(coreDataPolicy.getPaymentCoreData(any(), eq(paymentBO))).thenReturn(PaymentCoreDataPolicyHelper.getPaymentCoreDataInternal(paymentBO));
        when(paymentService.executePayment(any(), any())).thenReturn(TransactionStatusBO.ACSP);
        when(scaResponseResolver.updatePaymentRelatedResponseFields(any(), any())).thenAnswer(i -> localResolver.updatePaymentRelatedResponseFields((SCAPaymentResponseTO) i.getArguments()[0], (PaymentBO) i.getArguments()[1]));

        FieldSetter.setField(middlewareService, middlewareService.getClass().getDeclaredField("paymentProductsConfig"), getPaymentConfig());

        // When
        SCAPaymentResponseTO result = middlewareService.initiatePayment(buildScaInfoTO(), paymentTO, PaymentTypeTO.SINGLE);

        // Then
        assertNotNull(result);
    }

    @Test
    void initPmtRejectByCurrencyFail_NullEur2Accs() throws NoSuchFieldException {
        // Given
        //Payment: debtor - null / amount - EUR // Account - EUR/USD
        PaymentTO paymentTO = getPayment(null, USD);
        PaymentBO paymentBO = pmtMapper.toPaymentBO(paymentTO);
        paymentBO.setTransactionStatus(TransactionStatusBO.ACSC);
        paymentBO.setPaymentProduct("instant-sepa-credit-transfers");

        when(paymentConverter.toPaymentBO(any(PaymentTO.class), any())).thenReturn(paymentBO);

        FieldSetter.setField(middlewareService, middlewareService.getClass().getDeclaredField("paymentProductsConfig"), getPaymentConfig());

        // Then
        assertThrows(MiddlewareModuleException.class, () -> middlewareService.initiatePayment(buildScaInfoTO(), paymentTO, PaymentTypeTO.SINGLE));
    }

    @Test
    void initPmtRejectByCurrencyFail_UsdEurEur() throws NoSuchFieldException {
        // Given
        //Payment: debtor - USD / amount - EUR // Account - EUR
        PaymentTO paymentTO = getPayment(USD, EUR);
        PaymentBO paymentBO = pmtMapper.toPaymentBO(paymentTO);
        paymentBO.setTransactionStatus(TransactionStatusBO.ACSC);
        paymentBO.setPaymentProduct("instant-sepa-credit-transfers");

        FieldSetter.setField(middlewareService, middlewareService.getClass().getDeclaredField("paymentProductsConfig"), getPaymentConfig());

        when(paymentConverter.toPaymentBO(any(PaymentTO.class), any())).thenReturn(paymentBO);

        // Then
        assertThrows(MiddlewareModuleException.class, () -> middlewareService.initiatePayment(buildScaInfoTO(), paymentTO, PaymentTypeTO.SINGLE));

    }

    @Test
    void initPmtRejectByCurrencyFail_BlockedAccount() throws NoSuchFieldException {
        // Given
        //Payment: debtor - USD / amount - EUR // Account - EUR
        PaymentTO paymentTO = getPayment(USD, EUR);
        PaymentBO paymentBO = pmtMapper.toPaymentBO(paymentTO);
        paymentBO.setTransactionStatus(TransactionStatusBO.ACSC);
        paymentBO.setPaymentProduct("instant-sepa-credit-transfers");

        FieldSetter.setField(middlewareService, middlewareService.getClass().getDeclaredField("paymentProductsConfig"), getPaymentConfig());

        when(paymentConverter.toPaymentBO(any(PaymentTO.class), any())).thenReturn(paymentBO);

        // Then
        assertThrows(MiddlewareModuleException.class, () -> middlewareService.initiatePayment(buildScaInfoTO(), paymentTO, PaymentTypeTO.SINGLE));
    }


    @Test
    void executePayment_Success() {
        // Given
        PaymentBO paymentBO = readYml(PaymentBO.class, SINGLE_BO);
        BearerTokenBO bearerTokenBO = new BearerTokenBO();
        when(paymentService.getPaymentById(PAYMENT_ID)).thenReturn(paymentBO);
        when(coreDataPolicy.getPaymentCoreData(any(), eq(paymentBO))).thenReturn(PaymentCoreDataPolicyHelper.getPaymentCoreDataInternal(paymentBO));
        when(operationService.validateAuthCode(anyString(), anyString(), anyString(), anyInt())).thenReturn(new ScaValidationBO("authCode", true, ScaStatusBO.FINALISED, 0));
        when(authorizationService.consentToken(any(), any())).thenReturn(bearerTokenBO);
        when(operationService.authenticationCompleted(PAYMENT_ID, OpTypeBO.PAYMENT)).thenReturn(Boolean.TRUE);
        when(bearerTokenMapper.toBearerTokenTO(bearerTokenBO)).thenReturn(new BearerTokenTO());
        UserBO userBO = readYml(UserBO.class, "user1.yml");
        when(scaUtils.userBO(USER_ID)).thenReturn(userBO);
        when(paymentService.executePayment(anyString(), anyString())).thenReturn(TransactionStatusBO.ACSC);
        when(scaResponseResolver.updatePaymentRelatedResponseFields(any(), any())).thenAnswer(i -> localResolver.updatePaymentRelatedResponseFields((SCAPaymentResponseTO) i.getArguments()[0], (PaymentBO) i.getArguments()[1]));

        // When
        SCAPaymentResponseTO scaPaymentResponseTO = middlewareService.authorizePayment(buildScaInfoTO(), PAYMENT_ID);

        // Then
        assertNotNull(scaPaymentResponseTO);
    }

    @Test
    void executePayment_Failure() {
        // Given
        PaymentBO payment = readYml(PaymentBO.class, SINGLE_BO);
        UserBO userBO = readYml(UserBO.class, "user1.yml");
        when(scaUtils.userBO(USER_ID)).thenReturn(userBO);
        when(paymentService.getPaymentById(PAYMENT_ID)).thenReturn(payment);
        when(coreDataPolicy.getPaymentCoreData(any(), eq(payment))).thenReturn(PaymentCoreDataPolicyHelper.getPaymentCoreDataInternal(payment));
        when(operationService.validateAuthCode(any(), any(), any(), anyInt())).thenThrow(ScaModuleException.builder().build());

        // Then
        assertThrows(ScaModuleException.class, () -> middlewareService.authorizePayment(buildScaInfoTO(), PAYMENT_ID));
    }

    @Test
    void initiatePaymentCancellation() {
        // Given
        UserBO userBO = readYml(UserBO.class, "user1.yml");
        PaymentBO paymentBO = readYml(PaymentBO.class, SINGLE_BO);
        when(paymentService.getPaymentById(any())).thenReturn(paymentBO);
        when(paymentService.cancelPayment(PAYMENT_ID)).thenReturn(TransactionStatusBO.CANC);
        when(scaUtils.userBO(USER_ID)).thenReturn(userBO);
        when(scaResponseResolver.updatePaymentRelatedResponseFields(any(), any())).thenAnswer(i -> localResolver.updatePaymentRelatedResponseFields((SCAPaymentResponseTO) i.getArguments()[0], (PaymentBO) i.getArguments()[1]));
        when(coreDataPolicy.getPaymentCoreData(any(), eq(paymentBO))).thenReturn(PaymentCoreDataPolicyHelper.getPaymentCoreDataInternal(paymentBO));

        // When
        SCAPaymentResponseTO initiatePaymentCancellation = middlewareService.initiatePaymentCancellation(buildScaInfoTO(), PAYMENT_ID);

        // Then
        assertNotNull(initiatePaymentCancellation);
    }

    @Test
    void initiatePaymentCancellation_Failure_user_NF() {
        // Given
        when(scaUtils.userBO(USER_ID)).thenThrow(MiddlewareModuleException.class);

        assertThrows(MiddlewareModuleException.class, () -> middlewareService.initiatePaymentCancellation(buildScaInfoTO(), PAYMENT_ID));
    }

    @Test
    void initiatePaymentCancellation_Failure_pmt_NF() {
        // Given
        when(paymentService.getPaymentById(any())).thenThrow(DepositModuleException.class);

        // Then
        assertThrows(DepositModuleException.class, () -> middlewareService.initiatePaymentCancellation(buildScaInfoTO(), PAYMENT_ID));
    }

    @Test
    void initiatePaymentCancellation_Failure_pmt_and_acc_no_equal_iban() {
        // Given
        when(paymentService.getPaymentById(any())).thenThrow(DepositModuleException.class);

        // Then
        assertThrows(DepositModuleException.class, () -> middlewareService.initiatePaymentCancellation(buildScaInfoTO(), PAYMENT_ID));

    }

    @Test
    void initiatePaymentCancellation_Failure_pmt_status_acsc() {
        // Given
        PaymentBO payment = readYml(PaymentBO.class, "PaymentSingleBoStatusAcsc.yml");
        when(paymentService.getPaymentById(any())).thenReturn(payment);

        // Then
        assertThrows(MiddlewareModuleException.class, () -> middlewareService.initiatePaymentCancellation(buildScaInfoTO(), PAYMENT_ID));

    }

    @Test
    void getPendingPeriodicPayments() {
        // Given
        when(scaUtils.userBO(anyString())).thenReturn(new UserBO());
        when(paymentConverter.toPaymentTOList(any())).thenReturn(Collections.singletonList(new PaymentTO()));
        when(paymentService.getPaymentsByTypeStatusAndDebtor(eq(PaymentTypeBO.PERIODIC), eq(ACSP), anyList())).thenReturn(Collections.singletonList(new PaymentBO()));

        // When
        List<PaymentTO> result = middlewareService.getPendingPeriodicPayments(buildScaInfoTO());

        // Then
        assertTrue(result.size() > 0);
    }

    @Test
    void loadSCAForPaymentData() {
        // Given
        when(operationService.loadAuthCode(anyString())).thenReturn(getScaOperation());
        when(scaUtils.userBO(anyString())).thenReturn(new UserBO());
        when(coreDataPolicy.getPaymentCoreData(any(), any())).thenReturn(getPaymentCoreData());
        when(paymentService.getPaymentById(anyString())).thenReturn(getPaymentBO());
        when(accessService.resolveScaWeightByDebtorAccount(any(), anyString())).thenReturn(100);
        when(bearerTokenMapper.toBearerTokenTO(any())).thenReturn(new BearerTokenTO());
        when(authorizationService.consentToken(any(), any())).thenReturn(new BearerTokenBO());
        when(scaInfoMapper.toScaInfoBO(any())).thenReturn(new ScaInfoBO());

        when(scaResponseResolver.updatePaymentRelatedResponseFields(any(), any())).thenReturn(new SCAPaymentResponseTO());

        // When
        SCAPaymentResponseTO response = middlewareService.loadSCAForPaymentData(buildScaInfoTO(), PAYMENT_ID);

        // Then
        assertEquals(new SCAPaymentResponseTO(), response);
    }

    private PaymentCoreDataTO getPaymentCoreData() {
        PaymentCoreDataTO data = new PaymentCoreDataTO();
        data.setPaymentType(PaymentTypeTO.SINGLE.name());
        return data;
    }

    private PaymentBO getPaymentBO() {
        PaymentBO bo = new PaymentBO();
        AccountReferenceBO ref = new AccountReferenceBO();
        ref.setIban(IBAN);
        bo.setDebtorAccount(ref);
        return bo;
    }

    private SCAOperationBO getScaOperation() {
        SCAOperationBO bo = new SCAOperationBO();
        bo.setScaStatus(ScaStatusBO.PSUIDENTIFIED);
        return bo;
    }

    private static <T> T readYml(Class<T> aClass, String fileName) {
        try {
            return YamlReader.getInstance().getObjectFromResource(PaymentConverter.class, fileName, aClass);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private ScaInfoTO buildScaInfoTO() {
        ScaInfoTO info = new ScaInfoTO();
        info.setUserId(USER_ID);
        info.setAuthorisationId(AUTHORISATION_ID);
        info.setScaId(SCA_ID);
        info.setUserRole(UserRoleTO.CUSTOMER);
        info.setAuthCode(AUTH_CODE);
        info.setScaMethodId(SCA_METHOD_ID);
        info.setUserLogin(USER_LOGIN);
        return info;
    }

    private PaymentProductsConfig getPaymentConfig() {
        PaymentProductsConfig config = new PaymentProductsConfig();
        config.setInstant(new HashSet<>(Arrays.asList("sepa-credit-transfers", "instant-sepa-credit-transfers", "target-2-payments", "cross-border-credit-transfers")));
        return config;
    }

    private List<DepositAccountBO> getAccounts(boolean isBlocked, Currency... currency) {
        return Arrays.stream(currency)
                       .map(c -> getAccount(c, isBlocked))
                       .collect(Collectors.toList());
    }

    private DepositAccountBO getAccount(Currency currency, boolean isBlocked) {
        return DepositAccountBO.builder()
                       .iban(IBAN)
                       .currency(currency)
                       .blocked(isBlocked)
                       .build();
    }

    private PaymentTO getPayment(Currency payerCur, Currency amountCur) {
        PaymentTO payment = new PaymentTO();
        payment.setPaymentProduct("sepa-credit-transfers");
        payment.setPaymentType(PaymentTypeTO.SINGLE);
        payment.setDebtorAccount(getReference(payerCur));
        PaymentTargetTO target = new PaymentTargetTO();
        target.setInstructedAmount(getAmount(amountCur));
        target.setCreditorAccount(getReference(payerCur));
        payment.setTargets(Collections.singletonList(target));
        return payment;
    }

    private AmountTO getAmount(Currency amountCur) {
        return new AmountTO(amountCur, BigDecimal.TEN);
    }

    private AccountReferenceTO getReference(Currency payerCur) {
        return new AccountReferenceTO(IBAN, null, null, null, null, payerCur);
    }
}
