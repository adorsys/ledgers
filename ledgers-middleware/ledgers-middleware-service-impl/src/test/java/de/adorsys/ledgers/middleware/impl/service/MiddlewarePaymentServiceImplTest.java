package de.adorsys.ledgers.middleware.impl.service;

import static org.hamcrest.core.Is.is;
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
import de.adorsys.ledgers.deposit.api.service.DepositAccountPaymentService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.middleware.api.domain.payment.SinglePaymentTO;
import de.adorsys.ledgers.middleware.api.domain.payment.TransactionStatusTO;
import de.adorsys.ledgers.middleware.api.domain.um.AccessTokenTO;
import de.adorsys.ledgers.middleware.api.exception.AccountMiddlewareUncheckedException;
import de.adorsys.ledgers.middleware.api.exception.PaymentNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.PaymentProcessingMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.SCAOperationExpiredMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.SCAOperationNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.SCAOperationUsedOrStolenMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.SCAOperationValidationMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.UserNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.impl.converter.AuthCodeDataConverter;
import de.adorsys.ledgers.middleware.impl.converter.PaymentConverter;
import de.adorsys.ledgers.sca.service.SCAOperationService;
import pro.javatar.commons.reader.YamlReader;

@RunWith(MockitoJUnitRunner.class)
public class MiddlewarePaymentServiceImplTest {
    private static final String PAYMENT_ID = "myPaymentId";
    private static final String OP_ID = "opId";
    private static final String OP_DATA = "opData";

    private static final String SINGLE_BO = "PaymentSingle.yml";
    private static final String SINGLE_TO = "PaymentSingleTO.yml";
    private static final String WRONG_PAYMENT_ID = "wrong id";
    private static final String SYSTEM = "System";

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


//    @Test
//    public void generateAuthCode() throws AuthCodeGenerationMiddlewareException, AuthCodeGenerationException, SCAMethodNotSupportedException, SCAMethodNotSupportedMiddleException, UserNotFoundException, UserScaDataNotFoundException, UserNotFoundMiddlewareException, UserScaDataNotFoundMiddlewareException, SCAOperationValidationException, SCAOperationNotFoundException, PaymentNotFoundMiddlewareException, SCAOperationValidationMiddlewareException, SCAOperationNotFoundMiddlewareException, PaymentNotFoundException {
//
////        AuthCodeDataTO codeDataTO = readYml(AuthCodeDataTO.class, "auth-code-data.yml");
//        AuthCodeDataBO codeDataBO = readYml(AuthCodeDataBO.class, "auth-code-data.yml");
//        UserBO userBO = readYml(UserBO.class, "user1.yml");
//        UserTO userTO = readYml(UserTO.class, "user1.yml");
//        SCAOperationBO scaOperationBO = readYml(SCAOperationBO.class, "scaOperationEntity.yml");
//        String scaMethodId = "1";
//        String paymentId = "2222";
//        String authorisationId = "1111";
//
//        when(scaUtils.userBO()).thenReturn(userBO);
//        when(scaUtils.user(userBO)).thenReturn(userTO);
//        when(operationService.generateAuthCode(codeDataBO, userBO, ScaStatusBO.SCAMETHODSELECTED)).thenReturn(scaOperationBO);
//		when(scaUtils.getScaMethod(userTO, scaMethodId)).thenReturn(userTO.getScaUserData().iterator().next());
////        when(codeDataConverter.toAuthCodeDataBO(codeDataTO)).thenReturn(codeDataBO);
//		when(paymentService.getPaymentById(paymentId)).thenReturn(new PaymentBO());
//		
//		SCAPaymentResponseTO responseTO = middlewareService.selectSCAMethodForPayment(paymentId, authorisationId, scaMethodId);
//
//        assertThat(responseTO.getPaymentId(), is(paymentId));
//
////        verify(codeDataConverter, times(1)).toAuthCodeDataBO(codeDataTO);
//        verify(operationService, times(1)).generateAuthCode(codeDataBO, userBO, ScaStatusBO.SCAMETHODSELECTED);
//    }
//
//    @Test(expected = AuthCodeGenerationMiddlewareException.class)
//    public void generateAuthCodeWithException() throws AuthCodeGenerationMiddlewareException, AuthCodeGenerationException, SCAMethodNotSupportedException, SCAMethodNotSupportedMiddleException, UserNotFoundException, UserScaDataNotFoundException, UserNotFoundMiddlewareException, UserScaDataNotFoundMiddlewareException {
//        AuthCodeDataTO codeDataTO = readYml(AuthCodeDataTO.class, "auth-code-data.yml");
//        AuthCodeDataBO codeDataBO = readYml(AuthCodeDataBO.class, "auth-code-data.yml");
//
//        when(codeDataConverter.toAuthCodeDataBO(codeDataTO)).thenReturn(codeDataBO);
//        when(operationService.generateAuthCode(codeDataBO)).thenThrow(new AuthCodeGenerationException());
//
//        middlewareService.generateAuthCode(codeDataTO);
//    }

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

//    @Test
//    public void initiatePayment() throws AccountNotFoundMiddlewareException, NoAccessMiddlewareException, UserNotFoundMiddlewareException {
//        when(paymentConverter.toPaymentBO(any(), any())).thenReturn(readYml(PaymentBO.class, SINGLE_BO));
//        when(paymentService.initiatePayment(any(), any())).thenReturn(readYml(PaymentBO.class, SINGLE_BO));
//        when(userManagementService.findByUserLogin(anyString())).thenReturn(readYml(UserTO.class, "user2.yml"));
//        when(accessToken.getActor()).thenReturn("login");
//        when(paymentConverter.toPaymentTO(any())).thenReturn(readYml(SinglePaymentTO.class, SINGLE_TO));
//
//        Object result = middlewareService.initiatePayment(readYml(SinglePaymentTO.class, SINGLE_TO), PaymentTypeTO.SINGLE);
//        assertThat(result).isNotNull();
//    }
//
//    @Test
//    public void executePayment_Success() throws PaymentProcessingMiddlewareException, PaymentNotFoundException, PaymentProcessingException {
//        when(principal.getName()).thenReturn(SYSTEM);
//        when(paymentService.executePayment(any(), eq(SYSTEM))).thenReturn(TransactionStatusBO.ACSP);
//
//        TransactionStatusTO result = middlewareService.executePayment(PAYMENT_ID);
//        assertThat(result).isNotNull();
//    }

    @Test(expected = SCAOperationValidationMiddlewareException.class)
    public void executePayment_Failure() throws PaymentProcessingMiddlewareException, PaymentNotFoundException, SCAOperationNotFoundMiddlewareException, SCAOperationValidationMiddlewareException, SCAOperationExpiredMiddlewareException, SCAOperationUsedOrStolenMiddlewareException, PaymentNotFoundMiddlewareException {
        when(paymentService.getPaymentById(PAYMENT_ID)).thenReturn(readYml(PaymentBO.class, SINGLE_BO));
        String authorisationId = "authId";
		String authCode = "123456";
		middlewareService.authorizePayment(PAYMENT_ID, authorisationId, authCode);
    }
//
//    @Test
//    public void initiatePaymentCancellation() throws UserNotFoundMiddlewareException, PaymentNotFoundMiddlewareException, PaymentProcessingMiddlewareException, PaymentNotFoundException {
//        when(userManagementService.findById(any())).thenReturn(readYml(UserTO.class, "user2.yml"));
//        when(paymentService.getPaymentById(any())).thenReturn(readYml(PaymentBO.class, SINGLE_BO));
//
//        PaymentCancellationResponseTO result = middlewareService.initiatePaymentCancellation("PSU_ID", PAYMENT_ID);
//
//        assertThat(result).isNotNull();
//        assertThat(result).isEqualToComparingFieldByFieldRecursively(readYml(PaymentCancellationResponseTO.class, "CancellationResponse.yml"));
//    }
//
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