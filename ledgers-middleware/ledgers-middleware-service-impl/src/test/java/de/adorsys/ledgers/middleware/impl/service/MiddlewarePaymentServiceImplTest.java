package de.adorsys.ledgers.middleware.impl.service;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import de.adorsys.ledgers.deposit.api.domain.PaymentBO;
import de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO;
import de.adorsys.ledgers.deposit.api.exception.PaymentNotFoundException;
import de.adorsys.ledgers.deposit.api.exception.PaymentProcessingException;
import de.adorsys.ledgers.deposit.api.service.DepositAccountPaymentService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTypeTO;
import de.adorsys.ledgers.middleware.api.domain.payment.SinglePaymentTO;
import de.adorsys.ledgers.middleware.api.domain.payment.TransactionStatusTO;
import de.adorsys.ledgers.middleware.api.domain.sca.SCAPaymentResponseTO;
import de.adorsys.ledgers.middleware.api.domain.um.AccessTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.exception.AccountMiddlewareUncheckedException;
import de.adorsys.ledgers.middleware.api.exception.AccountNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.AuthCodeGenerationMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.NoAccessMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.PaymentNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.PaymentProcessingMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.SCAMethodNotSupportedMiddleException;
import de.adorsys.ledgers.middleware.api.exception.SCAOperationExpiredMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.SCAOperationNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.SCAOperationUsedOrStolenMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.SCAOperationValidationMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.UserNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.UserScaDataNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.impl.converter.AuthCodeDataConverter;
import de.adorsys.ledgers.middleware.impl.converter.BearerTokenMapper;
import de.adorsys.ledgers.middleware.impl.converter.PaymentConverter;
import de.adorsys.ledgers.sca.domain.OpTypeBO;
import de.adorsys.ledgers.sca.domain.SCAOperationBO;
import de.adorsys.ledgers.sca.domain.ScaStatusBO;
import de.adorsys.ledgers.sca.exception.AuthCodeGenerationException;
import de.adorsys.ledgers.sca.exception.SCAMethodNotSupportedException;
import de.adorsys.ledgers.sca.exception.SCAOperationExpiredException;
import de.adorsys.ledgers.sca.exception.SCAOperationNotFoundException;
import de.adorsys.ledgers.sca.exception.SCAOperationUsedOrStolenException;
import de.adorsys.ledgers.sca.exception.SCAOperationValidationException;
import de.adorsys.ledgers.sca.service.SCAOperationService;
import de.adorsys.ledgers.um.api.domain.BearerTokenBO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.exception.InsufficientPermissionException;
import de.adorsys.ledgers.um.api.exception.UserNotFoundException;
import de.adorsys.ledgers.um.api.exception.UserScaDataNotFoundException;
import de.adorsys.ledgers.um.api.service.UserService;
import pro.javatar.commons.reader.YamlReader;

@RunWith(MockitoJUnitRunner.class)
public class MiddlewarePaymentServiceImplTest {
    private static final String PAYMENT_ID = "myPaymentId";

    private static final String SINGLE_BO = "PaymentSingle.yml";
    private static final String SINGLE_TO = "PaymentSingleTO.yml";
    private static final String WRONG_PAYMENT_ID = "wrong id";
    private static final String SYSTEM = "System";

    private static String email = "userId@mail.de";
    private static String authCode = "123456";
    private static String authorisationId = "authorisationId";
    private static String template = "The TAN for your one time transfer order to Rozetka.ua at date 12-12-2018; account DE91100000000123456789; EUR 100 is: %s";
    
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

    @Test
    public void getPaymentStatusById() throws PaymentNotFoundMiddlewareException, PaymentNotFoundException {

        when(paymentService.getPaymentStatusById(PAYMENT_ID)).thenReturn(TransactionStatusBO.RJCT);

        TransactionStatusTO paymentResult = middlewareService.getPaymentStatusById(PAYMENT_ID);

        assertThat(paymentResult.getName(), is(TransactionStatusBO.RJCT.getName()));

        verify(paymentService, times(1)).getPaymentStatusById(PAYMENT_ID);
    }

    @Test(expected = PaymentNotFoundMiddlewareException.class)
    public void getPaymentStatusByIdWithException() throws PaymentNotFoundMiddlewareException, PaymentNotFoundException {

        when(paymentService.getPaymentStatusById(PAYMENT_ID)).thenThrow(new PaymentNotFoundException());

        middlewareService.getPaymentStatusById(PAYMENT_ID);

        verify(paymentService, times(1)).getPaymentStatusById(PAYMENT_ID);
    }


    @Test
    public void generateAuthCode() throws AuthCodeGenerationMiddlewareException, AuthCodeGenerationException, SCAMethodNotSupportedException, SCAMethodNotSupportedMiddleException, UserNotFoundException, UserScaDataNotFoundException, UserNotFoundMiddlewareException, UserScaDataNotFoundMiddlewareException, SCAOperationValidationException, SCAOperationNotFoundException, PaymentNotFoundMiddlewareException, SCAOperationValidationMiddlewareException, SCAOperationNotFoundMiddlewareException, PaymentNotFoundException {
        UserBO userBO = readYml(UserBO.class, "user1.yml");
        UserTO userTO = readYml(UserTO.class, "user1.yml");
        SCAOperationBO scaOperationBO = readYml(SCAOperationBO.class, "scaOperationEntity.yml");
        String scaMethodId = "1";
        String paymentId = "myPaymentId";
        String authorisationId = "1111";

        when(scaUtils.userBO()).thenReturn(userBO);
        when(scaUtils.user(userBO)).thenReturn(userTO);
        when(operationService.generateAuthCode(any(), any(), any())).thenReturn(scaOperationBO);
        when(paymentService.getPaymentById(paymentId)).thenReturn(readYml(PaymentBO.class, SINGLE_BO));
		SCAPaymentResponseTO responseTO = middlewareService.selectSCAMethodForPayment(paymentId, authorisationId, scaMethodId);

        assertThat(responseTO.getPaymentId(), is(paymentId));
    }

    @Test
    public void getPaymentById() throws PaymentNotFoundMiddlewareException, PaymentNotFoundException {
        when(paymentService.getPaymentById(PAYMENT_ID)).thenReturn(readYml(PaymentBO.class, SINGLE_BO));
        when(paymentConverter.toPaymentTO(any())).thenReturn(readYml(SinglePaymentTO.class, SINGLE_TO));
        SinglePaymentTO result = (SinglePaymentTO)middlewareService.getPaymentById(PAYMENT_ID);
        
        Assert.assertNotNull(result);
    }

    @Test(expected = PaymentNotFoundMiddlewareException.class)
    public void getPaymentById_Fail_wrong_id() throws PaymentNotFoundException, PaymentNotFoundMiddlewareException {
        when(paymentService.getPaymentById(WRONG_PAYMENT_ID))
                .thenThrow(new PaymentNotFoundException(WRONG_PAYMENT_ID));
        middlewareService.getPaymentById(WRONG_PAYMENT_ID);
    }

    @Test
    public void initiatePayment() throws AccountNotFoundMiddlewareException, NoAccessMiddlewareException, UserNotFoundMiddlewareException, PaymentNotFoundException {
        when(paymentConverter.toPaymentBO(any(), any())).thenReturn(readYml(PaymentBO.class, SINGLE_BO));
        when(paymentService.initiatePayment(any(), any())).thenReturn(readYml(PaymentBO.class, SINGLE_BO));
//        when(userManagementService.findByUserLogin(anyString())).thenReturn(readYml(UserTO.class, "user2.yml"));
        when(accessToken.getActor()).thenReturn("login");
//        when(paymentConverter.toPaymentTO(any())).thenReturn(readYml(SinglePaymentTO.class, SINGLE_TO));
        when(paymentService.updatePaymentStatusToAuthorised(PAYMENT_ID)).thenReturn(TransactionStatusBO.ACSP);
        Object result = middlewareService.initiatePayment(readYml(SinglePaymentTO.class, SINGLE_TO), PaymentTypeTO.SINGLE);
        assertNotNull(result);
    }

    @Test
    public void executePayment_Success() throws PaymentProcessingMiddlewareException, PaymentNotFoundException, PaymentProcessingException, SCAOperationNotFoundMiddlewareException, SCAOperationValidationMiddlewareException, SCAOperationExpiredMiddlewareException, SCAOperationUsedOrStolenMiddlewareException, PaymentNotFoundMiddlewareException, SCAOperationNotFoundException, SCAOperationValidationException, SCAOperationUsedOrStolenException, SCAOperationExpiredException, InsufficientPermissionException {
    	UserTO userTo = new UserTO("userId", email, "123456");
    	BearerTokenBO bearerTokenBO = new BearerTokenBO();
        when(accessToken.getActor()).thenReturn(SYSTEM);
        when(paymentService.getPaymentById(PAYMENT_ID)).thenReturn(readYml(PaymentBO.class, SINGLE_BO));
        when(operationService.validateAuthCode(authorisationId, PAYMENT_ID,template, authCode)).thenReturn(Boolean.TRUE);
        when(userService.grant(any(), any())).thenReturn(bearerTokenBO);
        when(operationService.authenticationCompleted(PAYMENT_ID, OpTypeBO.PAYMENT)).thenReturn(Boolean.FALSE);
        when(bearerTokenMapper.toBearerTokenTO(bearerTokenBO)).thenReturn(new BearerTokenTO());
		when(scaUtils.user()).thenReturn(userTo);
        SCAOperationBO scaOperation = scaOperation();
		when(scaUtils.loadAuthCode(authorisationId)).thenReturn(scaOperation);
		SCAPaymentResponseTO scaPaymentResponseTO = middlewareService.authorizePayment(PAYMENT_ID, authorisationId, authCode);
        assertNotNull(scaPaymentResponseTO);
    }

	private SCAOperationBO scaOperation() {
		SCAOperationBO scaOperation = new SCAOperationBO();
        scaOperation.setScaStatus(ScaStatusBO.EXEMPTED);
		return scaOperation;
	}

    @Test(expected = SCAOperationValidationMiddlewareException.class)
    public void executePayment_Failure() throws PaymentProcessingMiddlewareException, PaymentNotFoundException, SCAOperationNotFoundMiddlewareException, SCAOperationValidationMiddlewareException, SCAOperationExpiredMiddlewareException, SCAOperationUsedOrStolenMiddlewareException, PaymentNotFoundMiddlewareException {
        when(paymentService.getPaymentById(PAYMENT_ID)).thenReturn(readYml(PaymentBO.class, SINGLE_BO));
        String authorisationId = "authId";
		String authCode = "123456";
		middlewareService.authorizePayment(PAYMENT_ID, authorisationId, authCode);
    }

    @Test
    public void initiatePaymentCancellation() throws UserNotFoundMiddlewareException, PaymentNotFoundMiddlewareException, PaymentProcessingMiddlewareException, PaymentNotFoundException {
        when(paymentService.getPaymentById(any())).thenReturn(readYml(PaymentBO.class, SINGLE_BO));
        when(paymentService.cancelPayment(PAYMENT_ID)).thenReturn(TransactionStatusBO.CANC);
        SCAPaymentResponseTO initiatePaymentCancellation = middlewareService.initiatePaymentCancellation(PAYMENT_ID);

        assertNotNull(initiatePaymentCancellation);
    }

    @Test(expected = AccountMiddlewareUncheckedException.class)
    public void initiatePaymentCancellation_Failure_user_NF() throws PaymentNotFoundMiddlewareException, PaymentProcessingMiddlewareException {
        when(scaUtils.userBO()).thenThrow(AccountMiddlewareUncheckedException.class);

        middlewareService.initiatePaymentCancellation(PAYMENT_ID);
    }

    @Test(expected = PaymentNotFoundMiddlewareException.class)
    public void initiatePaymentCancellation_Failure_pmt_NF() throws UserNotFoundMiddlewareException, PaymentNotFoundMiddlewareException, PaymentProcessingMiddlewareException, PaymentNotFoundException {
        when(paymentService.getPaymentById(any())).thenThrow(new PaymentNotFoundException());
        middlewareService.initiatePaymentCancellation(PAYMENT_ID);
    }

    @Test(expected = PaymentNotFoundMiddlewareException.class)
    public void initiatePaymentCancellation_Failure_pmt_and_acc_no_equal_iban() throws UserNotFoundMiddlewareException, PaymentNotFoundMiddlewareException, PaymentProcessingMiddlewareException, PaymentNotFoundException {
        when(paymentService.getPaymentById(any())).thenThrow(PaymentNotFoundException.class);
        middlewareService.initiatePaymentCancellation(PAYMENT_ID);
    }

    @Test(expected = PaymentProcessingMiddlewareException.class)
    public void initiatePaymentCancellation_Failure_pmt_status_acsc() throws UserNotFoundMiddlewareException, PaymentNotFoundMiddlewareException, PaymentProcessingMiddlewareException, PaymentNotFoundException {
        when(paymentService.getPaymentById(any())).thenReturn(readYml(PaymentBO.class, "PaymentSingleBoStatusAcsc.yml"));
        middlewareService.initiatePaymentCancellation(PAYMENT_ID);
    }

    private static <T> T readYml(Class<T> aClass, String fileName) {
        try {
            return YamlReader.getInstance().getObjectFromResource(PaymentConverter.class, fileName, aClass);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}