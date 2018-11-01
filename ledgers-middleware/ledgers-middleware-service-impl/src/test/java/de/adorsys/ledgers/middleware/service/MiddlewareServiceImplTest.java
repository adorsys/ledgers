package de.adorsys.ledgers.middleware.service;

import de.adorsys.ledgers.deposit.api.domain.*;
import de.adorsys.ledgers.deposit.api.exception.DepositAccountNotFoundException;
import de.adorsys.ledgers.deposit.api.exception.PaymentNotFoundException;
import de.adorsys.ledgers.deposit.api.service.DepositAccountPaymentService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.middleware.converter.AccountConverter;
import de.adorsys.ledgers.middleware.converter.PaymentConverter;
import de.adorsys.ledgers.middleware.service.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.service.domain.payment.*;
import de.adorsys.ledgers.middleware.service.domain.payment.PaymentResultTO;
import de.adorsys.ledgers.middleware.service.domain.payment.TransactionStatusTO;
import de.adorsys.ledgers.middleware.service.exception.*;
import de.adorsys.ledgers.sca.exception.*;
import de.adorsys.ledgers.middleware.service.exception.*;
import de.adorsys.ledgers.sca.exception.AuthCodeGenerationException;
import de.adorsys.ledgers.sca.exception.SCAOperationNotFoundException;
import de.adorsys.ledgers.sca.exception.SCAOperationValidationException;
import de.adorsys.ledgers.sca.service.SCAOperationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import pro.javatar.commons.reader.YamlReader;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MiddlewareServiceImplTest {
    private static final String PAYMENT_ID = "myPaymentId";
    private static final String OP_ID = "opId";
    private static final String OP_DATA = "opData";
    private static final int VALIDITY_SECONDS = 60;
    private static final String ACCOUNT_ID = "id";
    private static final String PATH_SINGLE_BO = "de/adorsys/ledgers/middleware/converter/PaymentSingle.yml";
    private static final String PATH_SINGLE_TO = "de/adorsys/ledgers/middleware/converter/PaymentSingleTO.yml";
    private static final String PATH_PERIODIC_BO = "de/adorsys/ledgers/middleware/converter/PaymentPeriodic.yml";
    private static final String PATH_PERIODIC_TO = "de/adorsys/ledgers/middleware/converter/PaymentPeriodicTO.yml";
    private static final String PATH_BULK_BO = "de/adorsys/ledgers/middleware/converter/PaymentBulk.yml";
    private static final String PATH_BULK_TO = "de/adorsys/ledgers/middleware/converter/PaymentBulkTO.yml";
    private static final String WRONG_PAYMENT_ID = "wrong id";

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
    private AccountConverter accountConverter;

    @SuppressWarnings("unchecked")
    @Test
    public void getPaymentStatusById() throws PaymentNotFoundMiddlewareException, PaymentNotFoundException {
        PaymentResultBO<TransactionStatusBO> paymentResultBO = mock(PaymentResultBO.class);
        PaymentResultTO<TransactionStatusTO> paymentResultTO = new PaymentResultTO<>(TransactionStatusTO.RJCT);

        when(paymentService.getPaymentStatusById(PAYMENT_ID)).thenReturn(paymentResultBO);
        when(paymentConverter.toPaymentResultTO(paymentResultBO)).thenReturn(paymentResultTO);

        PaymentResultTO<TransactionStatusTO> paymentResult = middlewareService.getPaymentStatusById(PAYMENT_ID);

        assertThat(paymentResult.getPaymentResult().getName(), is(TransactionStatusBO.RJCT.getName()));

        verify(paymentService, times(1)).getPaymentStatusById(PAYMENT_ID);
        verify(paymentConverter, times(1)).toPaymentResultTO(paymentResultBO);
    }

    @Test(expected = PaymentNotFoundMiddlewareException.class)
    public void getPaymentStatusByIdWithException() throws PaymentNotFoundMiddlewareException, PaymentNotFoundException {

        when(paymentService.getPaymentStatusById(PAYMENT_ID)).thenThrow(new PaymentNotFoundException());

        middlewareService.getPaymentStatusById(PAYMENT_ID);

        verify(paymentService, times(1)).getPaymentStatusById(PAYMENT_ID);
    }


    @Test
    public void generateAuthCode() throws AuthCodeGenerationMiddlewareException, AuthCodeGenerationException {

        String myAuthCode = "my auth code";
        when(operationService.generateAuthCode(OP_ID, OP_DATA, VALIDITY_SECONDS)).thenReturn(myAuthCode);

        String authCode = middlewareService.generateAuthCode(OP_ID, OP_DATA, VALIDITY_SECONDS);

        assertThat(authCode, is(myAuthCode));

        verify(operationService, times(1)).generateAuthCode(OP_ID, OP_DATA, VALIDITY_SECONDS);
    }

    @Test(expected = AuthCodeGenerationMiddlewareException.class)
    public void generateAuthCodeWithException() throws AuthCodeGenerationMiddlewareException, AuthCodeGenerationException {

        when(operationService.generateAuthCode(OP_ID, OP_DATA, VALIDITY_SECONDS)).thenThrow(new AuthCodeGenerationException());

        middlewareService.generateAuthCode(OP_ID, OP_DATA, VALIDITY_SECONDS);
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
    public void getAccountDetailsByAccountId() throws DepositAccountNotFoundException, AccountNotFoundMiddlewareException {
        when(accountService.getDepositAccountById(any())).thenReturn(getAccount(DepositAccountBO.class));
        when(accountConverter.toAccountDetailsTO(any(), anyList())).thenReturn(getAccount(AccountDetailsTO.class));
        AccountDetailsTO details = middlewareService.getAccountDetailsByAccountId(ACCOUNT_ID);

        assertThat(details).isNotNull();
        verify(accountService, times(1)).getDepositAccountById(ACCOUNT_ID);
    }

    @Test(expected = AccountNotFoundMiddlewareException.class)
    public void getAccountDetailsByAccountId_wrong_id() throws AccountNotFoundMiddlewareException, DepositAccountNotFoundException {
        when(accountService.getDepositAccountById(any())).thenThrow(DepositAccountNotFoundException.class);
        AccountDetailsTO details = middlewareService.getAccountDetailsByAccountId("wrong id");

        verify(accountService, times(1)).getDepositAccountById(ACCOUNT_ID);
    }

    @Test
    public void getPaymentById() throws PaymentNotFoundMiddlewareException, PaymentNotFoundException {
        when(paymentConverter.toPaymentTypeBO(PaymentTypeTO.SINGLE)).thenReturn(PaymentTypeBO.SINGLE);
        when(paymentConverter.toPaymentProductBO(PaymentProductTO.SEPA)).thenReturn(PaymentProductBO.SEPA);
        when(paymentService.getPaymentById(PaymentTypeBO.SINGLE, PaymentProductBO.SEPA, PAYMENT_ID)).thenReturn(getPayment(PaymentBO.class, PATH_SINGLE_BO));
        when(paymentConverter.toPaymentTO(any())).thenReturn(getPayment(SinglePaymentTO.class, PATH_SINGLE_TO));
        Object result = middlewareService.getPaymentById(PaymentTypeTO.SINGLE, PaymentProductTO.SEPA, PAYMENT_ID);

        assertThat(result).isNotNull();
        assertThat(result).isEqualToComparingFieldByFieldRecursively(getPayment(SinglePaymentTO.class, PATH_SINGLE_TO));
    }

    @Test(expected = PaymentNotFoundMiddlewareException.class)
    public void getPaymentById_Fail_wrong_id() throws PaymentNotFoundException, PaymentNotFoundMiddlewareException {
        when(paymentConverter.toPaymentTypeBO(PaymentTypeTO.SINGLE)).thenReturn(PaymentTypeBO.SINGLE);
        when(paymentConverter.toPaymentProductBO(PaymentProductTO.SEPA)).thenReturn(PaymentProductBO.SEPA);
        when(paymentService.getPaymentById(PaymentTypeBO.SINGLE, PaymentProductBO.SEPA, WRONG_PAYMENT_ID))
                .thenThrow(new PaymentNotFoundException(WRONG_PAYMENT_ID));
        middlewareService.getPaymentById(PaymentTypeTO.SINGLE, PaymentProductTO.SEPA, WRONG_PAYMENT_ID);
    }

    private static <T> T getAccount(Class<T> aClass) {
        try {
            return YamlReader.getInstance().getObjectFromFile("de/adorsys/ledgers/middleware/converter/AccountDetails.yml", aClass);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Resource file not found", e);
        }
    }

    private static <T> T getPayment(Class<T> aClass, String path) {
        try {
            return YamlReader.getInstance().getObjectFromFile(path, aClass);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Resource file not found", e);
        }
    }
}