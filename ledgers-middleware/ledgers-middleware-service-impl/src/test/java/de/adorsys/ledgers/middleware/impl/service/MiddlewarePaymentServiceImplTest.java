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
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.junit.MockitoJUnitRunner;
import pro.javatar.commons.reader.YamlReader;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO.ACSP;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MiddlewarePaymentServiceImplTest {
    private static final String PAYMENT_ID = "myPaymentId";
    private static final String SINGLE_BO = "PaymentSingle.yml";
    private static final String SINGLE_TO = "PaymentSingleTO.yml";
    private static final String WRONG_PAYMENT_ID = "wrong id";
    private static final String USER_ID = "kjk345knkj45";
    private static final String SCA_ID = "scaId";
    private static final String SCA_METHOD_ID = "scaMethodId";
    private static final String EMAIL = "userId@mail.de";
    private static final String AUTH_CODE = "123456";
    private static final String AUTHORISATION_ID = "authorisationId";
    private static final String EMAIL_TEMPLATE = "The TAN for your one time sepa-credit-transfers order to Rozetka.ua at date 12-12-2018; account DE91100000000123456789; EUR 100 is: %s";
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

    private ScaResponseResolver localResolver = new ScaResponseResolver(scaUtils, new ScaChallengeDataResolverImpl(Arrays.asList(new EmailScaChallengeData())), Mappers.getMapper(UserMapper.class), operationService);

    private final PaymentConverter pmtMapper = Mappers.getMapper(PaymentConverter.class);

    @Test
    public void getPaymentStatusById() {

        when(paymentService.getPaymentStatusById(PAYMENT_ID)).thenReturn(TransactionStatusBO.RJCT);

        TransactionStatusTO paymentResult = middlewareService.getPaymentStatusById(PAYMENT_ID);

        assertThat(paymentResult.getName(), is(TransactionStatusBO.RJCT.getName()));

        verify(paymentService, times(1)).getPaymentStatusById(PAYMENT_ID);
    }

    @Test(expected = DepositModuleException.class)
    public void getPaymentStatusByIdWithException() {

        when(paymentService.getPaymentStatusById(PAYMENT_ID)).thenThrow(DepositModuleException.class);

        middlewareService.getPaymentStatusById(PAYMENT_ID);

        verify(paymentService, times(1)).getPaymentStatusById(PAYMENT_ID);
    }


    @Test
    public void generateAuthCode() {
        UserBO userBO = readYml(UserBO.class, "user1.yml");
        String paymentId = "myPaymentId";
        PaymentBO payment = readYml(PaymentBO.class, SINGLE_BO);

        when(scaUtils.userBO(USER_ID)).thenReturn(userBO);
        when(paymentService.getPaymentById(paymentId)).thenReturn(payment);
        when(coreDataPolicy.getPaymentCoreData(any(), eq(payment))).thenReturn(PaymentCoreDataPolicyHelper.getPaymentCoreDataInternal(payment));
        when(scaResponseResolver.updatePaymentRelatedResponseFields(any(), any())).thenAnswer(i -> localResolver.updatePaymentRelatedResponseFields((SCAPaymentResponseTO) i.getArguments()[0], (PaymentBO) i.getArguments()[1]));
        SCAPaymentResponseTO responseTO = middlewareService.selectSCAMethodForPayment(buildScaInfoTO(), paymentId);

        assertThat(responseTO.getPaymentId(), is(paymentId));
    }

    @Test
    public void getPaymentById() {
        when(paymentService.getPaymentById(PAYMENT_ID)).thenReturn(readYml(PaymentBO.class, SINGLE_BO));
        when(paymentConverter.toPaymentTO(any())).thenReturn(readYml(SinglePaymentTO.class, SINGLE_TO));
        SinglePaymentTO result = (SinglePaymentTO) middlewareService.getPaymentById(PAYMENT_ID);

        Assert.assertNotNull(result);
    }

    @Test(expected = DepositModuleException.class)
    public void getPaymentById_Fail_wrong_id() {
        when(paymentService.getPaymentById(WRONG_PAYMENT_ID)).thenThrow(DepositModuleException.class);
        middlewareService.getPaymentById(WRONG_PAYMENT_ID);
    }

    @Test
    public void initiatePayment() {
        UserBO userBO = readYml(UserBO.class, "user1.yml");
        PaymentBO paymentBO = readYml(PaymentBO.class, SINGLE_BO);
        when(coreDataPolicy.getPaymentCoreData(any(), eq(paymentBO))).thenReturn(PaymentCoreDataPolicyHelper.getPaymentCoreDataInternal(paymentBO));

        when(paymentConverter.toPaymentBO(any(PaymentTO.class), any())).thenReturn(paymentBO);
        when(accountService.getAccountsByIbanAndParamCurrency(any(), any())).thenReturn(Collections.singletonList(new DepositAccountBO("", paymentBO.getDebtorAccount().getIban(), null, null, null, null, paymentBO.getDebtorAccount().getCurrency(), null, null, null, AccountStatusBO.ENABLED, null, null, null, null)));
        when(paymentService.initiatePayment(any(), any())).thenReturn(paymentBO);
        when(paymentService.executePayment(any(), any())).thenReturn(TransactionStatusBO.ACSP);
        when(bearerTokenMapper.toBearerTokenTO(any())).thenReturn(new BearerTokenTO());
        when(scaUtils.userBO(USER_ID)).thenReturn(userBO);
        when(scaResponseResolver.updatePaymentRelatedResponseFields(any(), any())).thenAnswer(i -> localResolver.updatePaymentRelatedResponseFields((SCAPaymentResponseTO) i.getArguments()[0], (PaymentBO) i.getArguments()[1]));
        Whitebox.setInternalState(middlewareService, "paymentProductsConfig", getPaymentConfig());
        Object result = middlewareService.initiatePayment(buildScaInfoTO(), readYml(PaymentTO.class, SINGLE_BO), PaymentTypeTO.SINGLE);
        assertNotNull(result);
    }

    @Test(expected = MiddlewareModuleException.class)
    public void initiatePayment_creditor_account_disabled() {
        PaymentBO paymentBO = readYml(PaymentBO.class, SINGLE_BO);

        when(paymentConverter.toPaymentBO(any(PaymentTO.class), any())).thenReturn(paymentBO);
        when(accountService.getAccountsByIbanAndParamCurrency(any(), any())).thenReturn(Collections.singletonList(new DepositAccountBO("", paymentBO.getDebtorAccount().getIban(), null, null, null, null, paymentBO.getDebtorAccount().getCurrency(), null, null, null, AccountStatusBO.ENABLED, null, null, null, null)));
        when(accountService.getAccountsByIbanAndParamCurrency(eq("DE91100000000123456709"), any())).thenReturn(Collections.singletonList(new DepositAccountBO("", paymentBO.getTargets().iterator().next().getCreditorAccount().getIban(), null, null, null, null, paymentBO.getDebtorAccount().getCurrency(), null, null, null, AccountStatusBO.BLOCKED, null, null, null, null)));
        Whitebox.setInternalState(middlewareService, "paymentProductsConfig", getPaymentConfig());

        Object result = middlewareService.initiatePayment(buildScaInfoTO(), readYml(PaymentTO.class, SINGLE_BO), PaymentTypeTO.SINGLE);
        assertNotNull(result);
    }

    @Test(expected = MiddlewareModuleException.class)
    public void initiatePayment_invalid_amount() {
        PaymentBO paymentBO = readYml(PaymentBO.class, SINGLE_BO);
        paymentBO.getTargets().iterator().next().getInstructedAmount().setAmount(BigDecimal.ZERO);

        when(paymentConverter.toPaymentBO(any(PaymentTO.class), any())).thenReturn(paymentBO);

        Whitebox.setInternalState(middlewareService, "paymentProductsConfig", getPaymentConfig());
        Object result = middlewareService.initiatePayment(buildScaInfoTO(), readYml(PaymentTO.class, SINGLE_BO), PaymentTypeTO.SINGLE);
        assertNotNull(result);
    }

    @Test(expected = MiddlewareModuleException.class)
    public void initiatePayment_unsupported_product() {
        PaymentBO paymentBO = readYml(PaymentBO.class, SINGLE_BO);
        paymentBO.setPaymentProduct("zzz");

        when(paymentConverter.toPaymentBO(any(PaymentTO.class), any())).thenReturn(paymentBO);

        Whitebox.setInternalState(middlewareService, "paymentProductsConfig", getPaymentConfig());
        Object result = middlewareService.initiatePayment(buildScaInfoTO(), readYml(PaymentTO.class, SINGLE_BO), PaymentTypeTO.SINGLE);
        assertNotNull(result);
    }

    @Test
    public void initPmtRejectByCurrencySuccess() {
        //Payment: debtor - EUR / amount - EUR // Account - EUR
        PaymentTO paymentTO = getPayment(EUR, EUR);
        PaymentBO paymentBO = pmtMapper.toPaymentBO(paymentTO);
        paymentBO.setTransactionStatus(TransactionStatusBO.ACSC);
        paymentBO.setPaymentProduct("instant-sepa-credit-transfers");
        UserBO userBO = new UserBO("Test", "", "");

        when(accountService.getAccountsByIbanAndParamCurrency(any(), any())).thenReturn(getAccounts(AccountStatusBO.ENABLED, EUR));

        when(paymentConverter.toPaymentBO(any(PaymentTO.class), any())).thenReturn(paymentBO);
        when(scaUtils.userBO(USER_ID)).thenReturn(userBO);
        when(paymentService.initiatePayment(any(), any())).thenReturn(paymentBO);
        when(coreDataPolicy.getPaymentCoreData(any(), eq(paymentBO))).thenReturn(PaymentCoreDataPolicyHelper.getPaymentCoreDataInternal(paymentBO));
        when(paymentService.executePayment(any(), any())).thenReturn(TransactionStatusBO.ACSP);
        when(scaResponseResolver.updatePaymentRelatedResponseFields(any(), any())).thenAnswer(i -> localResolver.updatePaymentRelatedResponseFields((SCAPaymentResponseTO) i.getArguments()[0], (PaymentBO) i.getArguments()[1]));
        Whitebox.setInternalState(middlewareService, "paymentProductsConfig", getPaymentConfig());

        SCAPaymentResponseTO result = middlewareService.initiatePayment(buildScaInfoTO(), paymentTO, PaymentTypeTO.SINGLE);
        assertNotNull(result);
    }

    @Test(expected = MiddlewareModuleException.class)
    public void initPmtRejectByCurrencyFail_NullEur2Accs() {
        //Payment: debtor - null / amount - EUR // Account - EUR/USD
        PaymentTO paymentTO = getPayment(null, USD);
        PaymentBO paymentBO = pmtMapper.toPaymentBO(paymentTO);
        paymentBO.setTransactionStatus(TransactionStatusBO.ACSC);
        paymentBO.setPaymentProduct("instant-sepa-credit-transfers");

        when(paymentConverter.toPaymentBO(any(PaymentTO.class), any())).thenReturn(paymentBO);
        Whitebox.setInternalState(middlewareService, "paymentProductsConfig", getPaymentConfig());
        middlewareService.initiatePayment(buildScaInfoTO(), paymentTO, PaymentTypeTO.SINGLE);
    }

    @Test(expected = MiddlewareModuleException.class)
    public void initPmtRejectByCurrencyFail_UsdEurEur() {
        //Payment: debtor - USD / amount - EUR // Account - EUR
        PaymentTO paymentTO = getPayment(USD, EUR);
        PaymentBO paymentBO = pmtMapper.toPaymentBO(paymentTO);
        paymentBO.setTransactionStatus(TransactionStatusBO.ACSC);
        paymentBO.setPaymentProduct("instant-sepa-credit-transfers");

        Whitebox.setInternalState(middlewareService, "paymentProductsConfig", getPaymentConfig());

        when(paymentConverter.toPaymentBO(any(PaymentTO.class), any())).thenReturn(paymentBO);

        middlewareService.initiatePayment(buildScaInfoTO(), paymentTO, PaymentTypeTO.SINGLE);
    }

    @Test(expected = MiddlewareModuleException.class)
    public void initPmtRejectByCurrencyFail_BlockedAccount() {
        //Payment: debtor - USD / amount - EUR // Account - EUR
        PaymentTO paymentTO = getPayment(USD, EUR);
        PaymentBO paymentBO = pmtMapper.toPaymentBO(paymentTO);
        paymentBO.setTransactionStatus(TransactionStatusBO.ACSC);
        paymentBO.setPaymentProduct("instant-sepa-credit-transfers");

        Whitebox.setInternalState(middlewareService, "paymentProductsConfig", getPaymentConfig());
        when(paymentConverter.toPaymentBO(any(PaymentTO.class), any())).thenReturn(paymentBO);

        middlewareService.initiatePayment(buildScaInfoTO(), paymentTO, PaymentTypeTO.SINGLE);
    }


    private List<DepositAccountBO> getAccounts(AccountStatusBO status, Currency... currency) {
        return Arrays.stream(currency)
                       .map(c -> getAccount(c, status))
                       .collect(Collectors.toList());
    }

    private DepositAccountBO getAccount(Currency currency, AccountStatusBO status) {
        DepositAccountBO account = new DepositAccountBO();
        account.setIban(IBAN);
        account.setCurrency(currency);
        account.setAccountStatus(status);
        return account;
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

    @Test
    public void executePayment_Success() {
        PaymentBO paymentBO = readYml(PaymentBO.class, SINGLE_BO);
        BearerTokenBO bearerTokenBO = new BearerTokenBO();
        when(paymentService.getPaymentById(PAYMENT_ID)).thenReturn(paymentBO);
        when(coreDataPolicy.getPaymentCoreData(any(), eq(paymentBO))).thenReturn(PaymentCoreDataPolicyHelper.getPaymentCoreDataInternal(paymentBO));
        when(operationService.validateAuthCode(anyString(), anyString(), anyString(), anyString(), anyInt())).thenReturn(new ScaValidationBO("authCode", true, ScaStatusBO.FINALISED));
        when(authorizationService.consentToken(any(), any())).thenReturn(bearerTokenBO);
        when(operationService.authenticationCompleted(PAYMENT_ID, OpTypeBO.PAYMENT)).thenReturn(Boolean.FALSE);
        when(bearerTokenMapper.toBearerTokenTO(bearerTokenBO)).thenReturn(new BearerTokenTO());
        UserBO userBO = readYml(UserBO.class, "user1.yml");
        when(scaUtils.userBO(USER_ID)).thenReturn(userBO);
        when(scaResponseResolver.updatePaymentRelatedResponseFields(any(), any())).thenAnswer(i -> localResolver.updatePaymentRelatedResponseFields((SCAPaymentResponseTO) i.getArguments()[0], (PaymentBO) i.getArguments()[1]));
        SCAPaymentResponseTO scaPaymentResponseTO = middlewareService.authorizePayment(buildScaInfoTO(), PAYMENT_ID);
        assertNotNull(scaPaymentResponseTO);
    }

    @Test(expected = MiddlewareModuleException.class)
    public void executePayment_Failure() {
        PaymentBO payment = readYml(PaymentBO.class, SINGLE_BO);
        UserBO userBO = readYml(UserBO.class, "user1.yml");
        when(scaUtils.userBO(USER_ID)).thenReturn(userBO);
        when(paymentService.getPaymentById(PAYMENT_ID)).thenReturn(payment);
        when(coreDataPolicy.getPaymentCoreData(any(), eq(payment))).thenReturn(PaymentCoreDataPolicyHelper.getPaymentCoreDataInternal(payment));
        when(operationService.validateAuthCode(any(), any(), any(), any(), anyInt())).thenReturn(new ScaValidationBO("authCode", false, ScaStatusBO.FAILED));
        middlewareService.authorizePayment(buildScaInfoTO(), PAYMENT_ID);
    }

    @Test
    public void initiatePaymentCancellation() {
        UserBO userBO = readYml(UserBO.class, "user1.yml");
        PaymentBO paymentBO = readYml(PaymentBO.class, SINGLE_BO);
        when(paymentService.getPaymentById(any())).thenReturn(paymentBO);
        when(paymentService.cancelPayment(PAYMENT_ID)).thenReturn(TransactionStatusBO.CANC);
        when(scaUtils.userBO(USER_ID)).thenReturn(userBO);
        when(scaResponseResolver.updatePaymentRelatedResponseFields(any(), any())).thenAnswer(i -> localResolver.updatePaymentRelatedResponseFields((SCAPaymentResponseTO) i.getArguments()[0], (PaymentBO) i.getArguments()[1]));
        when(coreDataPolicy.getPaymentCoreData(any(), eq(paymentBO))).thenReturn(PaymentCoreDataPolicyHelper.getPaymentCoreDataInternal(paymentBO));
        SCAPaymentResponseTO initiatePaymentCancellation = middlewareService.initiatePaymentCancellation(buildScaInfoTO(), PAYMENT_ID);

        assertNotNull(initiatePaymentCancellation);
    }

    @Test(expected = MiddlewareModuleException.class)
    public void initiatePaymentCancellation_Failure_user_NF() {
        when(scaUtils.userBO(USER_ID)).thenThrow(MiddlewareModuleException.class);

        middlewareService.initiatePaymentCancellation(buildScaInfoTO(), PAYMENT_ID);
    }

    @Test(expected = DepositModuleException.class)
    public void initiatePaymentCancellation_Failure_pmt_NF() {
        when(paymentService.getPaymentById(any())).thenThrow(DepositModuleException.class);
        middlewareService.initiatePaymentCancellation(buildScaInfoTO(), PAYMENT_ID);
    }

    @Test(expected = DepositModuleException.class)
    public void initiatePaymentCancellation_Failure_pmt_and_acc_no_equal_iban() {
        when(paymentService.getPaymentById(any())).thenThrow(DepositModuleException.class);
        middlewareService.initiatePaymentCancellation(buildScaInfoTO(), PAYMENT_ID);
    }

    @Test(expected = MiddlewareModuleException.class)
    public void initiatePaymentCancellation_Failure_pmt_status_acsc() {
        PaymentBO payment = readYml(PaymentBO.class, "PaymentSingleBoStatusAcsc.yml");
        when(paymentService.getPaymentById(any())).thenReturn(payment);
        middlewareService.initiatePaymentCancellation(buildScaInfoTO(), PAYMENT_ID);
    }

    @Test
    public void getPendingPeriodicPayments() {
        when(scaUtils.userBO(anyString())).thenReturn(new UserBO());
        when(paymentConverter.toPaymentTOList(any())).thenReturn(Collections.singletonList(new PaymentTO()));
        when(paymentService.getPaymentsByTypeStatusAndDebtor(eq(PaymentTypeBO.PERIODIC), eq(ACSP), anyList())).thenReturn(Collections.singletonList(new PaymentBO()));
        List<PaymentTO> result = middlewareService.getPendingPeriodicPayments(buildScaInfoTO());
        assertThat(result.size() > 0, is(true));
    }

    @Test
    public void loadSCAForPaymentData() {
        when(operationService.loadAuthCode(anyString())).thenReturn(getScaOperation());
        when(scaUtils.userBO(anyString())).thenReturn(new UserBO());
        when(coreDataPolicy.getPaymentCoreData(any(), any())).thenReturn(getPaymentCoreData());
        when(paymentService.getPaymentById(anyString())).thenReturn(getPaymentBO());
        when(accessService.resolveScaWeightByDebtorAccount(any(), anyString())).thenReturn(100);
        when(bearerTokenMapper.toBearerTokenTO(any())).thenReturn(new BearerTokenTO());
        when(authorizationService.consentToken(any(), any())).thenReturn(new BearerTokenBO());
        when(scaInfoMapper.toScaInfoBO(any())).thenReturn(new ScaInfoBO());

        when(scaResponseResolver.updatePaymentRelatedResponseFields(any(), any())).thenReturn(new SCAPaymentResponseTO());
        SCAPaymentResponseTO response = middlewareService.loadSCAForPaymentData(buildScaInfoTO(), PAYMENT_ID);
        assertThat(response, is(new SCAPaymentResponseTO()));
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

    private static ScaInfoTO buildScaInfoTO() {
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
}
