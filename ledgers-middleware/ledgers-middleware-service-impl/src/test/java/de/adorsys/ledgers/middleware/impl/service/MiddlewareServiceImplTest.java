package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.deposit.api.domain.PaymentBO;
import de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO;
import de.adorsys.ledgers.deposit.api.exception.PaymentNotFoundException;
import de.adorsys.ledgers.deposit.api.exception.PaymentProcessingException;
import de.adorsys.ledgers.deposit.api.service.DepositAccountPaymentService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.middleware.api.domain.payment.*;
import de.adorsys.ledgers.middleware.api.domain.sca.AuthCodeDataTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.exception.*;
import de.adorsys.ledgers.middleware.impl.converter.AuthCodeDataConverter;
import de.adorsys.ledgers.middleware.impl.converter.PaymentConverter;
import de.adorsys.ledgers.sca.domain.AuthCodeDataBO;
import de.adorsys.ledgers.sca.exception.*;
import de.adorsys.ledgers.sca.service.SCAOperationService;
import de.adorsys.ledgers.um.api.exception.UserNotFoundException;
import de.adorsys.ledgers.um.api.exception.UserScaDataNotFoundException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import pro.javatar.commons.reader.YamlReader;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MiddlewareServiceImplTest {
    private static final String PAYMENT_ID = "myPaymentId";
    private static final String OP_ID = "opId";
    private static final String OP_DATA = "opData";

    private static final String SINGLE_BO = "PaymentSingle.yml";
    private static final String SINGLE_TO = "PaymentSingleTO.yml";
    private static final String WRONG_PAYMENT_ID = "wrong id";

    private static final LocalDateTime TIME = LocalDateTime.now();

    @InjectMocks
    private MiddlewareServiceImpl middlewareService;

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
    public void generateAuthCode() throws AuthCodeGenerationMiddlewareException, AuthCodeGenerationException, SCAMethodNotSupportedException, SCAMethodNotSupportedMiddleException, UserNotFoundException, UserScaDataNotFoundException, UserNotFoundMiddlewareException, UserScaDataNotFoundMiddlewareException {

        AuthCodeDataTO codeDataTO = readYml(AuthCodeDataTO.class, "auth-code-data.yml");
        AuthCodeDataBO codeDataBO = readYml(AuthCodeDataBO.class, "auth-code-data.yml");

        when(codeDataConverter.toAuthCodeDataBO(codeDataTO)).thenReturn(codeDataBO);
        when(operationService.generateAuthCode(codeDataBO)).thenReturn(OP_ID);

        String actualOpId = middlewareService.generateAuthCode(codeDataTO);

        assertThat(actualOpId, is(OP_ID));

        verify(codeDataConverter, times(1)).toAuthCodeDataBO(codeDataTO);
        verify(operationService, times(1)).generateAuthCode(codeDataBO);
    }

    @Test(expected = AuthCodeGenerationMiddlewareException.class)
    public void generateAuthCodeWithException() throws AuthCodeGenerationMiddlewareException, AuthCodeGenerationException, SCAMethodNotSupportedException, SCAMethodNotSupportedMiddleException, UserNotFoundException, UserScaDataNotFoundException, UserNotFoundMiddlewareException, UserScaDataNotFoundMiddlewareException {
        AuthCodeDataTO codeDataTO = readYml(AuthCodeDataTO.class, "auth-code-data.yml");
        AuthCodeDataBO codeDataBO = readYml(AuthCodeDataBO.class, "auth-code-data.yml");

        when(codeDataConverter.toAuthCodeDataBO(codeDataTO)).thenReturn(codeDataBO);
        when(operationService.generateAuthCode(codeDataBO)).thenThrow(new AuthCodeGenerationException());

        middlewareService.generateAuthCode(codeDataTO);
    }

    @Test
    public void validateAuthCode() throws SCAOperationValidationMiddlewareException, SCAOperationNotFoundMiddlewareException, SCAOperationNotFoundException, SCAOperationValidationException, SCAOperationUsedOrStolenException, SCAOperationExpiredException, SCAOperationExpiredMiddlewareException, SCAOperationUsedOrStolenMiddlewareException {
        String myAuthCode = "my auth code";

        when(operationService.validateAuthCode(OP_ID, OP_DATA, myAuthCode)).thenReturn(Boolean.TRUE);

        boolean valid = middlewareService.validateAuthCode(OP_ID, OP_DATA, myAuthCode);

        assertThat(valid, is(Boolean.TRUE));

        verify(operationService, times(1)).validateAuthCode(OP_ID, OP_DATA, myAuthCode);
    }

    @Test(expected = SCAOperationNotFoundMiddlewareException.class)
    public void validateAuthCodeWithNotFoundException() throws SCAOperationValidationMiddlewareException, SCAOperationNotFoundMiddlewareException, SCAOperationNotFoundException, SCAOperationValidationException, SCAOperationUsedOrStolenException, SCAOperationExpiredException, SCAOperationExpiredMiddlewareException, SCAOperationUsedOrStolenMiddlewareException {
        String myAuthCode = "my auth code";

        when(operationService.validateAuthCode(OP_ID, OP_DATA, myAuthCode)).thenThrow(new SCAOperationNotFoundException());

        middlewareService.validateAuthCode(OP_ID, OP_DATA, myAuthCode);
    }

    @Test(expected = SCAOperationValidationMiddlewareException.class)
    public void validateAuthCodeWithValidationException() throws SCAOperationValidationMiddlewareException, SCAOperationNotFoundMiddlewareException, SCAOperationNotFoundException, SCAOperationValidationException, SCAOperationUsedOrStolenException, SCAOperationExpiredException, SCAOperationExpiredMiddlewareException, SCAOperationUsedOrStolenMiddlewareException {
        String myAuthCode = "my auth code";

        when(operationService.validateAuthCode(OP_ID, OP_DATA, myAuthCode)).thenThrow(new SCAOperationValidationException());

        middlewareService.validateAuthCode(OP_ID, OP_DATA, myAuthCode);
    }

    @Test
    public void getPaymentById() throws PaymentNotFoundMiddlewareException, PaymentNotFoundException {
        when(paymentService.getPaymentById(PAYMENT_ID)).thenReturn(readYml(PaymentBO.class, SINGLE_BO));
        when(paymentConverter.toPaymentTO(any())).thenReturn(readYml(SinglePaymentTO.class, SINGLE_TO));
        Object result = middlewareService.getPaymentById(PaymentTypeTO.SINGLE, PaymentProductTO.SEPA, PAYMENT_ID);

        assertThat(result).isNotNull();
        assertThat(result).isEqualToComparingFieldByFieldRecursively(readYml(SinglePaymentTO.class, SINGLE_TO));
    }

    @Test(expected = PaymentNotFoundMiddlewareException.class)
    public void getPaymentById_Fail_wrong_id() throws PaymentNotFoundException, PaymentNotFoundMiddlewareException {
        when(paymentService.getPaymentById(WRONG_PAYMENT_ID))
                .thenThrow(new PaymentNotFoundException(WRONG_PAYMENT_ID));
        middlewareService.getPaymentById(PaymentTypeTO.SINGLE, PaymentProductTO.SEPA, WRONG_PAYMENT_ID);
    }

    @Test
    public void initiatePayment() throws AccountNotFoundMiddlewareException {
        when(paymentConverter.toPaymentBO(any(), any())).thenReturn(readYml(PaymentBO.class, SINGLE_BO));
        when(paymentService.initiatePayment(any())).thenReturn(readYml(PaymentBO.class, SINGLE_BO));
        when(paymentConverter.toPaymentTO(any())).thenReturn(readYml(SinglePaymentTO.class, SINGLE_TO));

        Object result = middlewareService.initiatePayment(readYml(SinglePaymentTO.class, SINGLE_TO), PaymentTypeTO.SINGLE);
        assertThat(result).isNotNull();
    }

    @Test
    public void executePayment_Success() throws PaymentProcessingMiddlewareException, PaymentNotFoundException, PaymentProcessingException {
        when(paymentService.executePayment(any())).thenReturn(TransactionStatusBO.ACSP);

        TransactionStatusTO result = middlewareService.executePayment(PAYMENT_ID);
        assertThat(result).isNotNull();
    }

    @Test(expected = PaymentProcessingMiddlewareException.class)
    public void executePayment_Failure() throws PaymentProcessingMiddlewareException, PaymentNotFoundException, PaymentProcessingException {
        when(paymentService.executePayment(any())).thenThrow(new PaymentNotFoundException());

        middlewareService.executePayment(PAYMENT_ID);
    }

    @Test
    public void initiatePaymentCancellation() throws UserNotFoundMiddlewareException, PaymentNotFoundMiddlewareException, PaymentProcessingMiddlewareException, PaymentNotFoundException {
        when(userManagementService.findById(any())).thenReturn(readYml(UserTO.class, "user2.yml"));
        when(paymentService.getPaymentById(any())).thenReturn(readYml(PaymentBO.class, SINGLE_BO));

        PaymentCancellationResponseTO result = middlewareService.initiatePaymentCancellation("PSU_ID", PAYMENT_ID);

        assertThat(result).isNotNull();
        assertThat(result).isEqualToComparingFieldByFieldRecursively(readYml(PaymentCancellationResponseTO.class, "CancellationResponse.yml"));
    }

    @Test(expected = UserNotFoundMiddlewareException.class)
    public void initiatePaymentCancellation_Failure_user_NF() throws UserNotFoundMiddlewareException, PaymentNotFoundMiddlewareException, PaymentProcessingMiddlewareException {
        when(userManagementService.findById(any())).thenThrow(new UserNotFoundMiddlewareException());
        middlewareService.initiatePaymentCancellation("PSU_ID", PAYMENT_ID);
    }

    @Test(expected = PaymentNotFoundMiddlewareException.class)
    public void initiatePaymentCancellation_Failure_pmt_NF() throws UserNotFoundMiddlewareException, PaymentNotFoundMiddlewareException, PaymentProcessingMiddlewareException, PaymentNotFoundException {
        when(userManagementService.findById(any())).thenReturn(readYml(UserTO.class, "user2.yml"));
        when(paymentService.getPaymentById(any())).thenThrow(new PaymentNotFoundException());
        middlewareService.initiatePaymentCancellation("PSU_ID", PAYMENT_ID);
    }

    @Test(expected = PaymentNotFoundMiddlewareException.class)
    public void initiatePaymentCancellation_Failure_pmt_and_acc_no_equal_iban() throws UserNotFoundMiddlewareException, PaymentNotFoundMiddlewareException, PaymentProcessingMiddlewareException, PaymentNotFoundException {
        when(userManagementService.findById(any())).thenReturn(readYml(UserTO.class, "randomUser.yml"));
        when(paymentService.getPaymentById(any())).thenReturn(readYml(PaymentBO.class, SINGLE_BO));
        middlewareService.initiatePaymentCancellation("PSU_ID", PAYMENT_ID);
    }

    @Test(expected = PaymentProcessingMiddlewareException.class)
    public void initiatePaymentCancellation_Failure_pmt_status_acsc() throws UserNotFoundMiddlewareException, PaymentNotFoundMiddlewareException, PaymentProcessingMiddlewareException, PaymentNotFoundException {
        when(userManagementService.findById(any())).thenReturn(readYml(UserTO.class, "user2.yml"));
        when(paymentService.getPaymentById(any())).thenReturn(readYml(PaymentBO.class, "PaymentSingleBoStatusAcsc.yml"));
        middlewareService.initiatePaymentCancellation("PSU_ID", PAYMENT_ID);
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