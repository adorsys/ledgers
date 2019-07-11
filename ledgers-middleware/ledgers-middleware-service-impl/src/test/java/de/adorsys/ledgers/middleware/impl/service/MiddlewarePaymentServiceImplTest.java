package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.deposit.api.domain.PaymentBO;
import de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO;
import de.adorsys.ledgers.deposit.api.exception.DepositModuleException;
import de.adorsys.ledgers.deposit.api.service.DepositAccountPaymentService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTypeTO;
import de.adorsys.ledgers.middleware.api.domain.payment.SinglePaymentTO;
import de.adorsys.ledgers.middleware.api.domain.payment.TransactionStatusTO;
import de.adorsys.ledgers.middleware.api.domain.sca.SCAPaymentResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.um.AccessTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.exception.AccountMiddlewareUncheckedException;
import de.adorsys.ledgers.middleware.api.exception.PaymentProcessingMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.SCAOperationValidationMiddlewareException;
import de.adorsys.ledgers.middleware.impl.converter.*;
import de.adorsys.ledgers.middleware.impl.policies.PaymentCancelPolicy;
import de.adorsys.ledgers.middleware.impl.policies.PaymentCoreDataPolicy;
import de.adorsys.ledgers.middleware.impl.policies.PaymentCoreDataPolicyHelper;
import de.adorsys.ledgers.sca.domain.OpTypeBO;
import de.adorsys.ledgers.sca.domain.SCAOperationBO;
import de.adorsys.ledgers.sca.domain.ScaStatusBO;
import de.adorsys.ledgers.sca.service.SCAOperationService;
import de.adorsys.ledgers.um.api.domain.BearerTokenBO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.service.UserService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import pro.javatar.commons.reader.YamlReader;

import java.io.IOException;

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
    private PaymentCancelPolicy cancelPolicy;
    @Mock
    private AccessTokenMapper accessTokenMapper;
    @Mock
    private AccessService accessService;
    @Mock
    private ScaInfoMapper scaInfoMapper;

    @Test
    public void getPaymentStatusById(){

        when(paymentService.getPaymentStatusById(PAYMENT_ID)).thenReturn(TransactionStatusBO.RJCT);

        TransactionStatusTO paymentResult = middlewareService.getPaymentStatusById(PAYMENT_ID);

        assertThat(paymentResult.getName(), is(TransactionStatusBO.RJCT.getName()));

        verify(paymentService, times(1)).getPaymentStatusById(PAYMENT_ID);
    }

    @Test(expected = DepositModuleException.class)
    public void getPaymentStatusByIdWithException()  {

        when(paymentService.getPaymentStatusById(PAYMENT_ID)).thenThrow(DepositModuleException.class);

        middlewareService.getPaymentStatusById(PAYMENT_ID);

        verify(paymentService, times(1)).getPaymentStatusById(PAYMENT_ID);
    }


    @Test
    public void generateAuthCode() {
        UserBO userBO = readYml(UserBO.class, "user1.yml");
        UserTO userTO = readYml(UserTO.class, "user1.yml");
        SCAOperationBO scaOperationBO = readYml(SCAOperationBO.class, "scaOperationEntity.yml");
        String paymentId = "myPaymentId";
    	PaymentBO payment = readYml(PaymentBO.class, SINGLE_BO);

        when(scaUtils.userBO(USER_ID)).thenReturn(userBO);
        when(scaUtils.user(userBO)).thenReturn(userTO);
        when(operationService.generateAuthCode(any(), any(), any())).thenReturn(scaOperationBO);
        when(paymentService.getPaymentById(paymentId)).thenReturn(payment);
		when(coreDataPolicy.getPaymentCoreData(payment)).thenReturn(PaymentCoreDataPolicyHelper.getPaymentCoreDataInternal(payment));
		SCAPaymentResponseTO responseTO = middlewareService.selectSCAMethodForPayment(buildScaInfoTO(), paymentId);

        assertThat(responseTO.getPaymentId(), is(paymentId));
    }

    @Test
    public void getPaymentById()  {
        when(paymentService.getPaymentById(PAYMENT_ID)).thenReturn(readYml(PaymentBO.class, SINGLE_BO));
        when(paymentConverter.toPaymentTO(any())).thenReturn(readYml(SinglePaymentTO.class, SINGLE_TO));
        SinglePaymentTO result = (SinglePaymentTO)middlewareService.getPaymentById(PAYMENT_ID);
        
        Assert.assertNotNull(result);
    }

    @Test(expected = DepositModuleException.class)
    public void getPaymentById_Fail_wrong_id() {
        when(paymentService.getPaymentById(WRONG_PAYMENT_ID))
                .thenThrow(DepositModuleException.class);
        middlewareService.getPaymentById(WRONG_PAYMENT_ID);
    }

    @Test
    public void initiatePayment() {
        UserBO userBO = readYml(UserBO.class, "user1.yml");
        UserTO userTO = readYml(UserTO.class, "user1.yml");
    	PaymentBO paymentBO = readYml(PaymentBO.class, SINGLE_BO);
		when(coreDataPolicy.getPaymentCoreData(paymentBO)).thenReturn(PaymentCoreDataPolicyHelper.getPaymentCoreDataInternal(paymentBO));

    	when(paymentConverter.toPaymentBO(any(), any())).thenReturn(paymentBO);
        when(paymentService.initiatePayment(any(), any())).thenReturn(paymentBO);
        when(paymentService.executePayment(any(), any())).thenReturn(TransactionStatusBO.ACSP);
        when(bearerTokenMapper.toBearerTokenTO(any())).thenReturn(new BearerTokenTO());
        when(scaUtils.userBO(USER_ID)).thenReturn(userBO);
        when(scaUtils.user(userBO)).thenReturn(userTO);
        Object result = middlewareService.initiatePayment(buildScaInfoTO(), readYml(SinglePaymentTO.class, SINGLE_TO), PaymentTypeTO.SINGLE);
        assertNotNull(result);
    }

    @Test
    public void executePayment_Success() throws  SCAOperationValidationMiddlewareException {
    	PaymentBO paymentBO = readYml(PaymentBO.class, SINGLE_BO);
    	BearerTokenBO bearerTokenBO = new BearerTokenBO();
        when(paymentService.getPaymentById(PAYMENT_ID)).thenReturn(paymentBO);
        when(coreDataPolicy.getPaymentCoreData(paymentBO)).thenReturn(PaymentCoreDataPolicyHelper.getPaymentCoreDataInternal(paymentBO));
        when(operationService.validateAuthCode(AUTHORISATION_ID, PAYMENT_ID, EMAIL_TEMPLATE, AUTH_CODE, 0)).thenReturn(Boolean.TRUE);
        when(userService.consentToken(any(), any())).thenReturn(bearerTokenBO);
        when(operationService.authenticationCompleted(PAYMENT_ID, OpTypeBO.PAYMENT)).thenReturn(Boolean.FALSE);
        when(bearerTokenMapper.toBearerTokenTO(bearerTokenBO)).thenReturn(new BearerTokenTO());
		when(scaUtils.user(USER_ID)).thenReturn(new UserTO(USER_ID, EMAIL, "123456"));
        SCAOperationBO scaOperation = scaOperation();
		when(scaUtils.loadAuthCode(AUTHORISATION_ID)).thenReturn(scaOperation);
        UserBO userBO = readYml(UserBO.class, "user1.yml");
        when(scaUtils.userBO(USER_ID)).thenReturn(userBO);
		SCAPaymentResponseTO scaPaymentResponseTO = middlewareService.authorizePayment(buildScaInfoTO(), PAYMENT_ID);
        assertNotNull(scaPaymentResponseTO);
    }

	private SCAOperationBO scaOperation() {
		SCAOperationBO scaOperation = new SCAOperationBO();
        scaOperation.setScaStatus(ScaStatusBO.EXEMPTED);
		return scaOperation;
	}

    @Test(expected = SCAOperationValidationMiddlewareException.class)
    public void executePayment_Failure() throws SCAOperationValidationMiddlewareException {
    	PaymentBO payment = readYml(PaymentBO.class, SINGLE_BO);
        UserBO userBO = readYml(UserBO.class, "user1.yml");
        when(scaUtils.userBO(USER_ID)).thenReturn(userBO);
        when(paymentService.getPaymentById(PAYMENT_ID)).thenReturn(payment);
		when(coreDataPolicy.getPaymentCoreData(payment)).thenReturn(PaymentCoreDataPolicyHelper.getPaymentCoreDataInternal(payment));
		middlewareService.authorizePayment(buildScaInfoTO(), PAYMENT_ID);
    }

    @Test
    public void initiatePaymentCancellation() throws  PaymentProcessingMiddlewareException {
        UserBO userBO = readYml(UserBO.class, "user1.yml");
        UserTO userTO = readYml(UserTO.class, "user1.yml");
    	PaymentBO paymentBO = readYml(PaymentBO.class, SINGLE_BO);
        when(paymentService.getPaymentById(any())).thenReturn(paymentBO);
        when(paymentService.cancelPayment(PAYMENT_ID)).thenReturn(TransactionStatusBO.CANC);
        when(coreDataPolicy.getCancelPaymentCoreData(paymentBO)).thenReturn(PaymentCoreDataPolicyHelper.getPaymentCoreDataInternal(paymentBO));
        when(scaUtils.userBO(USER_ID)).thenReturn(userBO);
        when(scaUtils.user(userBO)).thenReturn(userTO);
        SCAPaymentResponseTO initiatePaymentCancellation = middlewareService.initiatePaymentCancellation(buildScaInfoTO(), PAYMENT_ID);

        assertNotNull(initiatePaymentCancellation);
    }

    @Test(expected = AccountMiddlewareUncheckedException.class)
    public void initiatePaymentCancellation_Failure_user_NF() throws PaymentProcessingMiddlewareException {
        when(scaUtils.userBO(USER_ID)).thenThrow(AccountMiddlewareUncheckedException.class);

        middlewareService.initiatePaymentCancellation(buildScaInfoTO(), PAYMENT_ID);
    }

    @Test(expected = DepositModuleException.class)
    public void initiatePaymentCancellation_Failure_pmt_NF() throws PaymentProcessingMiddlewareException {
        when(paymentService.getPaymentById(any())).thenThrow(DepositModuleException.class);
        middlewareService.initiatePaymentCancellation(buildScaInfoTO(), PAYMENT_ID);
    }

    @Test(expected = DepositModuleException.class)
    public void initiatePaymentCancellation_Failure_pmt_and_acc_no_equal_iban() throws  PaymentProcessingMiddlewareException {
        when(paymentService.getPaymentById(any())).thenThrow(DepositModuleException.class);
        middlewareService.initiatePaymentCancellation(buildScaInfoTO(), PAYMENT_ID);
    }

    @Test(expected = PaymentProcessingMiddlewareException.class)
    public void initiatePaymentCancellation_Failure_pmt_status_acsc() throws PaymentProcessingMiddlewareException {
    	PaymentBO payment = readYml(PaymentBO.class, "PaymentSingleBoStatusAcsc.yml");
    	when(paymentService.getPaymentById(any())).thenReturn(payment);
        doThrow(PaymentProcessingMiddlewareException.class).when(cancelPolicy).onCancel(any(), any());
        middlewareService.initiatePaymentCancellation(buildScaInfoTO(), PAYMENT_ID);
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
}
