package de.adorsys.ledgers.middleware.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.adorsys.ledgers.deposit.api.domain.*;
import de.adorsys.ledgers.deposit.api.exception.DepositAccountNotFoundException;
import de.adorsys.ledgers.deposit.api.exception.PaymentNotFoundException;
import de.adorsys.ledgers.deposit.api.exception.PaymentProcessingException;
import de.adorsys.ledgers.deposit.api.exception.TransactionNotFoundException;
import de.adorsys.ledgers.deposit.api.service.DepositAccountPaymentService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.middleware.converter.AccountDetailsMapper;
import de.adorsys.ledgers.middleware.converter.PaymentConverter;
import de.adorsys.ledgers.middleware.converter.SCAMethodTOConverter;
import de.adorsys.ledgers.middleware.service.domain.account.AccountBalanceTO;
import de.adorsys.ledgers.middleware.service.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.service.domain.account.TransactionTO;
import de.adorsys.ledgers.middleware.service.domain.payment.PaymentProductTO;
import de.adorsys.ledgers.middleware.service.domain.payment.PaymentTypeTO;
import de.adorsys.ledgers.middleware.service.domain.payment.SinglePaymentTO;
import de.adorsys.ledgers.middleware.service.domain.payment.TransactionStatusTO;
import de.adorsys.ledgers.middleware.service.domain.sca.SCAMethodTO;
import de.adorsys.ledgers.middleware.service.domain.sca.SCAMethodTypeTO;
import de.adorsys.ledgers.middleware.service.exception.*;
import de.adorsys.ledgers.postings.api.exception.LedgerAccountNotFoundException;
import de.adorsys.ledgers.sca.exception.*;
import de.adorsys.ledgers.sca.service.SCAOperationService;
import de.adorsys.ledgers.um.api.domain.AccountAccessBO;
import de.adorsys.ledgers.um.api.domain.ScaUserDataBO;
import de.adorsys.ledgers.um.api.exception.UserNotFoundException;
import de.adorsys.ledgers.um.api.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import pro.javatar.commons.reader.YamlReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MiddlewareServiceImplTest {
    private static final String PAYMENT_ID = "myPaymentId";
    private static final String OP_ID = "opId";
    private static final String OP_DATA = "opData";
    private static final int VALIDITY_SECONDS = 60;
    private static final String ACCOUNT_ID = "id";
    private static final String ACCOUNT_DETAILS_TO = "AccountDetailsTO.yml";
    private static final String ACCOUNT_DETAILS_BO = "AccountDetails.yml";

    private static final String SINGLE_BO = "PaymentSingle.yml";
    private static final String SINGLE_TO = "PaymentSingleTO.yml";
    private static final String WRONG_PAYMENT_ID = "wrong id";
    private static final String USER_MESSAGE = "user message";

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
    private AccountDetailsMapper detailsMapper;

    @Mock
    private UserService userService;

    @Mock
    private SCAMethodTOConverter scaMethodTOConverter;

    private ScaUserDataBO userDataBO;
    private SCAMethodTO scaMethodTO;

    @Before
    public void setUp() {
        userDataBO = new ScaUserDataBO();
        scaMethodTO = new SCAMethodTO();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void getPaymentStatusById() throws PaymentNotFoundMiddlewareException, PaymentNotFoundException {
//        PaymentResultBO<TransactionStatusBO> paymentResultBO = mock(PaymentResultBO.class);
//        PaymentResultTO<TransactionStatusTO> paymentResultTO = new PaymentResultTO<>(TransactionStatusTO.RJCT);

        when(paymentService.getPaymentStatusById(PAYMENT_ID)).thenReturn(TransactionStatusBO.RJCT);
//        when(paymentConverter.toPaymentResultTO(paymentResultBO)).thenReturn(paymentResultTO);

        TransactionStatusTO paymentResult = middlewareService.getPaymentStatusById(PAYMENT_ID);

        assertThat(paymentResult.getName(), is(TransactionStatusBO.RJCT.getName()));

        verify(paymentService, times(1)).getPaymentStatusById(PAYMENT_ID);
//        verify(paymentConverter, times(1)).toPaymentResultTO(paymentResultBO);
    }

    @Test(expected = PaymentNotFoundMiddlewareException.class)
    public void getPaymentStatusByIdWithException() throws PaymentNotFoundMiddlewareException, PaymentNotFoundException {

        when(paymentService.getPaymentStatusById(PAYMENT_ID)).thenThrow(new PaymentNotFoundException());

        middlewareService.getPaymentStatusById(PAYMENT_ID);

        verify(paymentService, times(1)).getPaymentStatusById(PAYMENT_ID);
    }


    @Test
    public void generateAuthCode() throws AuthCodeGenerationMiddlewareException, AuthCodeGenerationException, SCAMethodNotSupportedException, SCAMethodNotSupportedMiddleException {

        when(scaMethodTOConverter.toScaUserDataBO(scaMethodTO)).thenReturn(userDataBO);
        when(operationService.generateAuthCode(OP_ID, userDataBO, OP_DATA, USER_MESSAGE, VALIDITY_SECONDS)).thenReturn(OP_ID);

        String actualOpId = middlewareService.generateAuthCode(OP_ID, scaMethodTO, OP_DATA, USER_MESSAGE, VALIDITY_SECONDS);

        assertThat(actualOpId, is(OP_ID));

        verify(scaMethodTOConverter, times(1)).toScaUserDataBO(scaMethodTO);
        verify(operationService, times(1)).generateAuthCode(OP_ID, userDataBO, OP_DATA, USER_MESSAGE, VALIDITY_SECONDS);
    }

    @Test(expected = AuthCodeGenerationMiddlewareException.class)
    public void generateAuthCodeWithException() throws AuthCodeGenerationMiddlewareException, AuthCodeGenerationException, SCAMethodNotSupportedException, SCAMethodNotSupportedMiddleException {
        when(scaMethodTOConverter.toScaUserDataBO(scaMethodTO)).thenReturn(userDataBO);
        when(operationService.generateAuthCode(OP_ID, userDataBO, OP_DATA, USER_MESSAGE, VALIDITY_SECONDS)).thenThrow(new AuthCodeGenerationException());

        middlewareService.generateAuthCode(OP_ID, scaMethodTO, OP_DATA, USER_MESSAGE, VALIDITY_SECONDS);
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
    public void getAccountDetailsByAccountId() throws DepositAccountNotFoundException, AccountNotFoundMiddlewareException, IOException, LedgerAccountNotFoundException {
        when(accountService.getDepositAccountById(any())).thenReturn(getAccount(DepositAccountBO.class));
        when(accountService.getBalances(any())).thenReturn(readBalances(BalanceBO.class));
        when(detailsMapper.toAccountDetailsTO(any(), any())).thenReturn(getAccount(AccountDetailsTO.class));
        AccountDetailsTO details = middlewareService.getAccountDetailsByAccountId(ACCOUNT_ID);

        assertThat(details).isNotNull();
        verify(accountService, times(1)).getDepositAccountById(ACCOUNT_ID);
    }

    @Test(expected = AccountNotFoundMiddlewareException.class)
    public void getAccountDetailsByAccountId_wrong_id() throws AccountNotFoundMiddlewareException, DepositAccountNotFoundException {
        when(accountService.getDepositAccountById(any())).thenThrow(new DepositAccountNotFoundException());
        AccountDetailsTO details = middlewareService.getAccountDetailsByAccountId("wrong id");

        verify(accountService, times(1)).getDepositAccountById(ACCOUNT_ID);
    }

    @Test
    public void getPaymentById() throws PaymentNotFoundMiddlewareException, PaymentNotFoundException {
        when(paymentConverter.toPaymentTypeBO(PaymentTypeTO.SINGLE)).thenReturn(PaymentTypeBO.SINGLE);
        when(paymentConverter.toPaymentProductBO(PaymentProductTO.SEPA)).thenReturn(PaymentProductBO.SEPA);
        when(paymentService.getPaymentById(PAYMENT_ID)).thenReturn(readYml(PaymentBO.class, SINGLE_BO));
        when(paymentConverter.toPaymentTO(any())).thenReturn(readYml(SinglePaymentTO.class, SINGLE_TO));
        Object result = middlewareService.getPaymentById(PaymentTypeTO.SINGLE, PaymentProductTO.SEPA, PAYMENT_ID);

        assertThat(result).isNotNull();
        assertThat(result).isEqualToComparingFieldByFieldRecursively(readYml(SinglePaymentTO.class, SINGLE_TO));
    }

    @Test(expected = PaymentNotFoundMiddlewareException.class)
    public void getPaymentById_Fail_wrong_id() throws PaymentNotFoundException, PaymentNotFoundMiddlewareException {
        when(paymentConverter.toPaymentTypeBO(PaymentTypeTO.SINGLE)).thenReturn(PaymentTypeBO.SINGLE);
        when(paymentConverter.toPaymentProductBO(PaymentProductTO.SEPA)).thenReturn(PaymentProductBO.SEPA);
        when(paymentService.getPaymentById(WRONG_PAYMENT_ID))
                .thenThrow(new PaymentNotFoundException(WRONG_PAYMENT_ID));
        middlewareService.getPaymentById(PaymentTypeTO.SINGLE, PaymentProductTO.SEPA, WRONG_PAYMENT_ID);
    }

    @Test
    public void getSCAMethods() throws UserNotFoundException, UserNotFoundMiddlewareException {
        String login = "spe@adorsys.com.ua";
        List<ScaUserDataBO> userData = getDataFromFile("SCAUserDataBO.yml", new TypeReference<List<ScaUserDataBO>>() {
        });
        List<SCAMethodTO> scaMethodTOS = getDataFromFile("SCAMethodTO.yml", new TypeReference<List<SCAMethodTO>>() {
        });

        when(userService.getUserScaData(login)).thenReturn(userData);
        when(scaMethodTOConverter.toSCAMethodListTO(userData)).thenReturn(scaMethodTOS);

        List<SCAMethodTO> scaMethods = middlewareService.getSCAMethods(login);

        assertThat(scaMethods.size(), is(2));

        assertThat(scaMethods.get(0).getType(), is(SCAMethodTypeTO.EMAIL));
        assertThat(scaMethods.get(0).getValue(), is("spe@adorsys.com.ua"));

        assertThat(scaMethods.get(1).getType(), is(SCAMethodTypeTO.MOBILE));
        assertThat(scaMethods.get(1).getValue(), is("+380933686868"));

        verify(userService, times(1)).getUserScaData(login);
        verify(scaMethodTOConverter, times(1)).toSCAMethodListTO(userData);
    }

    @Test(expected = UserNotFoundMiddlewareException.class)
    public void getSCAMethodsUserNotFound() throws UserNotFoundException, UserNotFoundMiddlewareException {
        String login = "spe@adorsys.com.ua";

        when(userService.getUserScaData(login)).thenThrow(new UserNotFoundException());

        middlewareService.getSCAMethods(login);
    }

    @Test
    public void initiatePayment() {
        when(paymentConverter.toPaymentBO(any(), any())).thenReturn(readYml(PaymentBO.class, SINGLE_BO));
        when(paymentService.initiatePayment(any())).thenReturn(readYml(PaymentBO.class, SINGLE_BO));
        when(paymentConverter.toPaymentTO(any())).thenReturn(readYml(SinglePaymentTO.class, SINGLE_TO));

        Object result = middlewareService.initiatePayment(readYml(SinglePaymentTO.class, SINGLE_TO), PaymentTypeTO.SINGLE);
        assertThat(result).isNotNull();
    }

    @Test
    public void getBalances_Success() throws AccountNotFoundMiddlewareException, LedgerAccountNotFoundException, IOException, DepositAccountNotFoundException {
        when(accountService.getDepositAccountById(ACCOUNT_ID)).thenReturn(readYml(DepositAccountBO.class, ACCOUNT_DETAILS_BO));
        when(accountService.getBalances(any())).thenReturn(readBalances(BalanceBO.class));
        when(detailsMapper.toAccountBalancesTO(any())).thenReturn(readBalances(AccountBalanceTO.class));

        List<AccountBalanceTO> balances = middlewareService.getBalances(ACCOUNT_ID);
        assertThat(balances).isNotEmpty();
        assertThat(balances.size()).isEqualTo(2);
    }

    @Test(expected = AccountNotFoundMiddlewareException.class)
    public void getBalances_Failure_depositAccount_Not_Found() throws AccountNotFoundMiddlewareException, DepositAccountNotFoundException {
        when(accountService.getDepositAccountById(ACCOUNT_ID)).thenThrow(new DepositAccountNotFoundException());
        middlewareService.getBalances(ACCOUNT_ID);
    }

    @Test(expected = AccountNotFoundMiddlewareException.class)
    public void getBalances_Failure_ledgerAccount_Not_Found() throws AccountNotFoundMiddlewareException, LedgerAccountNotFoundException, DepositAccountNotFoundException {
        when(accountService.getDepositAccountById(ACCOUNT_ID)).thenReturn(readYml(DepositAccountBO.class, ACCOUNT_DETAILS_BO));
        when(accountService.getBalances(any())).thenThrow(new LedgerAccountNotFoundException("id"));

        middlewareService.getBalances(ACCOUNT_ID);
    }

    @Test
    public void executePayment_Success() throws PaymentProcessingMiddlewareException, PaymentNotFoundException, PaymentProcessingException {
        when(paymentConverter.toPaymentProductBO(any())).thenReturn(PaymentProductBO.SEPA);
        when(paymentConverter.toPaymentTypeBO(any())).thenReturn(PaymentTypeBO.SINGLE);
        when(paymentService.executePayment(any())).thenReturn(TransactionStatusBO.ACSP);
//        when(paymentConverter.toTransactionTOList(any())).thenReturn(Collections.singletonList(readYml(TransactionTO.class, "TransactionTO.yml")));

        TransactionStatusTO result = middlewareService.executePayment(PAYMENT_ID);
        assertThat(result).isNotNull();
    }

    @Test(expected = PaymentProcessingMiddlewareException.class)
    public void executePayment_Failure() throws PaymentProcessingMiddlewareException, PaymentNotFoundException, PaymentProcessingException {
        when(paymentConverter.toPaymentProductBO(any())).thenReturn(PaymentProductBO.SEPA);
//        when(paymentConverter.toPaymentTypeBO(any())).thenReturn(PaymentTypeBO.SINGLE);
        when(paymentService.executePayment(any())).thenThrow(new PaymentNotFoundException());

        middlewareService.executePayment(PAYMENT_ID);
    }

    @Test
    public void getAllAccountDetailsByUserLogin() throws UserNotFoundMiddlewareException, UserNotFoundException {

        String userLogin = "spe";

        AccountDetailsTO account = getAccount(AccountDetailsTO.class);
        DepositAccountBO accountBO = getAccount(DepositAccountBO.class);

        List<AccountAccessBO> accessBOList = getDataFromFile("account-access-bo-list.yml", new TypeReference<List<AccountAccessBO>>() {
        });
        String iban = accessBOList.get(0).getIban();

        when(userService.getAccountAccessByUserLogin(userLogin)).thenReturn(accessBOList);
        when(accountService.getDepositAccountsByIBAN(Collections.singletonList(iban))).thenReturn(Collections.singletonList(accountBO));
        when(detailsMapper.toAccountDetailsListTO(Collections.singletonList(accountBO))).thenReturn(Collections.singletonList(account));

        List<AccountDetailsTO> details = middlewareService.getAllAccountDetailsByUserLogin(userLogin);

        assertThat(details.size(), is(1));
        assertThat(details.get(0), is(account));

        verify(userService, times(1)).getAccountAccessByUserLogin(userLogin);
        verify(accountService, times(1)).getDepositAccountsByIBAN(Collections.singletonList(iban));
        verify(detailsMapper, times(1)).toAccountDetailsListTO(Collections.singletonList(accountBO));
    }

    private static <T> T getAccount(Class<T> aClass) {
        try {
            return YamlReader.getInstance().getObjectFromFile("de/adorsys/ledgers/middleware/converter/AccountDetails.yml", aClass);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Resource file not found", e);
        }
    }

    @Test
    public void getTransactionById() throws TransactionNotFoundMiddlewareException, AccountNotFoundMiddlewareException, DepositAccountNotFoundException, TransactionNotFoundException {
        when(accountService.getTransactionById(anyString(), anyString())).thenReturn(readYml(TransactionDetailsBO.class, "TransactionBO.yml"));
        when(paymentConverter.toTransactionTO(any())).thenReturn(readYml(TransactionTO.class, "TransactionTO.yml"));

        TransactionTO result = middlewareService.getTransactionById("ACCOUNT_ID", "POSTING_ID");
        assertThat(result).isNotNull();
        assertThat(result).isEqualToComparingFieldByFieldRecursively(readYml(TransactionTO.class, "TransactionTO.yml"));
    }

    @Test(expected = TransactionNotFoundMiddlewareException.class)
    public void getTransactionById_Failure() throws TransactionNotFoundMiddlewareException, AccountNotFoundMiddlewareException, DepositAccountNotFoundException, TransactionNotFoundException {
        when(accountService.getTransactionById(anyString(), anyString())).thenThrow(new TransactionNotFoundException("ACCOUNT_ID", "POSTING_ID"));

        middlewareService.getTransactionById("ACCOUNT_ID", "POSTING_ID");
    }

    private static <T> List<T> readBalances(Class<T> tClass) throws IOException {
        return Arrays.asList(
                YamlReader.getInstance().getObjectFromResource(AccountDetailsMapper.class, "Balance1.yml", tClass),
                YamlReader.getInstance().getObjectFromResource(AccountDetailsMapper.class, "Balance2.yml", tClass)
        );
    }

    //    todo: replace by javatar-commons version 0.7
    private <T> T getDataFromFile(String fileName, TypeReference<T> typeReference) {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        objectMapper.findAndRegisterModules();
        InputStream inputStream = getClass().getResourceAsStream(fileName);
        try {
            return objectMapper.readValue(inputStream, typeReference);
        } catch (IOException e) {
            throw new IllegalStateException("File not found");
        }
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