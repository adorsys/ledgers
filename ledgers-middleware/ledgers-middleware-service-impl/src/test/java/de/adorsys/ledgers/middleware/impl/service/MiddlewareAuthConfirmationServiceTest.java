package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO;
import de.adorsys.ledgers.deposit.api.service.DepositAccountPaymentService;
import de.adorsys.ledgers.middleware.api.domain.payment.TransactionStatusTO;
import de.adorsys.ledgers.middleware.api.domain.sca.AuthConfirmationTO;
import de.adorsys.ledgers.sca.domain.OpTypeBO;
import de.adorsys.ledgers.sca.domain.ScaAuthConfirmationBO;
import de.adorsys.ledgers.sca.service.SCAOperationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO.PATC;
import static de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO.RJCT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
public class MiddlewareAuthConfirmationServiceTest {
    private static final String AUTH_ID = "auth_id";
    private static final String USER_LOGIN = "user_login";
    private static final String OPERATION_ID = "234lkjsdf9234";
    private static final String AUTH_CONFIRM_CODE = "auth_conf_code";

    @InjectMocks
    private MiddlewareAuthConfirmationServiceImpl middlewareUserService;

    @Mock
    private SCAOperationService scaOperationService;

    @Mock
    private DepositAccountPaymentService depositAccountPaymentService;

    @Test
    public void verifyAuthConfirmationCode_Payment() throws Exception {
        AuthConfirmationTO expected = new AuthConfirmationTO()
                                              .success(true)
                                              .transactionStatus(TransactionStatusTO.ACCP);
        whenNew(AuthConfirmationTO.class).withArguments(anyBoolean(), anyBoolean(), any(TransactionStatusTO.class), anyBoolean()).thenReturn(expected);

        when(scaOperationService.verifyAuthConfirmationCode(anyString(), anyString())).thenReturn(new ScaAuthConfirmationBO(true, OpTypeBO.PAYMENT, OPERATION_ID));
        when(scaOperationService.authenticationCompleted(anyString(), any(OpTypeBO.class))).thenReturn(true);
        when(depositAccountPaymentService.executePayment(anyString(), anyString())).thenReturn(TransactionStatusBO.ACCP);

        AuthConfirmationTO actual = middlewareUserService.verifyAuthConfirmationCode(AUTH_ID, AUTH_CONFIRM_CODE, USER_LOGIN);

        verify(scaOperationService, times(1)).verifyAuthConfirmationCode(anyString(), anyString());
        verify(scaOperationService, times(1)).authenticationCompleted(anyString(), any(OpTypeBO.class));
        verify(depositAccountPaymentService, times(1)).executePayment(anyString(), anyString());

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void verifyAuthConfirmationCode_Payment_Multilevel() throws Exception {
        ReflectionTestUtils.setField(middlewareUserService, "multilevelScaEnable", true);
        AuthConfirmationTO expected = new AuthConfirmationTO()
                                              .transactionStatus(TransactionStatusTO.PATC)
                                              .multilevelScaRequired(true)
                                              .partiallyAuthorised(true);
        whenNew(AuthConfirmationTO.class).withArguments(anyBoolean(), anyBoolean(), any(TransactionStatusTO.class), anyBoolean()).thenReturn(expected);

        when(scaOperationService.verifyAuthConfirmationCode(AUTH_ID, AUTH_CONFIRM_CODE)).thenReturn(new ScaAuthConfirmationBO(true, OpTypeBO.PAYMENT, OPERATION_ID));
        when(scaOperationService.authenticationCompleted(OPERATION_ID, OpTypeBO.PAYMENT)).thenReturn(false);
        when(depositAccountPaymentService.updatePaymentStatus(anyString(), any(TransactionStatusBO.class))).thenReturn(PATC);

        AuthConfirmationTO actual = middlewareUserService.verifyAuthConfirmationCode(AUTH_ID, AUTH_CONFIRM_CODE, USER_LOGIN);

        verify(scaOperationService, times(1)).verifyAuthConfirmationCode(anyString(), anyString());
        verify(scaOperationService, times(1)).authenticationCompleted(anyString(), any(OpTypeBO.class));
        verify(depositAccountPaymentService, times(0)).executePayment(anyString(), anyString());
        verify(depositAccountPaymentService, times(1)).updatePaymentStatus(anyString(), any(TransactionStatusBO.class));

        assertThat(actual.getTransactionStatus()).isEqualTo(expected.getTransactionStatus());
    }

    @Test
    public void verifyAuthConfirmationCode_Consent() throws Exception {
        AuthConfirmationTO expected = new AuthConfirmationTO()
                                              .success(true);
        whenNew(AuthConfirmationTO.class).withArguments(anyBoolean(), anyBoolean(), any(TransactionStatusTO.class), anyBoolean()).thenReturn(expected);

        when(scaOperationService.verifyAuthConfirmationCode(AUTH_ID, AUTH_CONFIRM_CODE)).thenReturn(new ScaAuthConfirmationBO(true, OpTypeBO.CONSENT, OPERATION_ID));
        when(scaOperationService.authenticationCompleted(OPERATION_ID, OpTypeBO.CONSENT)).thenReturn(true);

        AuthConfirmationTO actual = middlewareUserService.verifyAuthConfirmationCode(AUTH_ID, AUTH_CONFIRM_CODE, USER_LOGIN);

        verify(scaOperationService, times(1)).verifyAuthConfirmationCode(anyString(), anyString());
        verify(scaOperationService, times(1)).authenticationCompleted(anyString(), any(OpTypeBO.class));
        verify(depositAccountPaymentService, times(0)).executePayment(anyString(), anyString());
        verify(depositAccountPaymentService, times(0)).updatePaymentStatus(anyString(), any(TransactionStatusBO.class));

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void verifyAuthConfirmationCode_Consent_Multilevel() throws Exception {
        ReflectionTestUtils.setField(middlewareUserService, "multilevelScaEnable", true);
        AuthConfirmationTO expected = new AuthConfirmationTO()
                                              .success(true)
                                              .partiallyAuthorised(true)
                                              .multilevelScaRequired(true);
        whenNew(AuthConfirmationTO.class).withArguments(anyBoolean(), anyBoolean(), any(TransactionStatusTO.class), anyBoolean()).thenReturn(expected);

        when(scaOperationService.verifyAuthConfirmationCode(AUTH_ID, AUTH_CONFIRM_CODE)).thenReturn(new ScaAuthConfirmationBO(true, OpTypeBO.CONSENT, OPERATION_ID));
        when(scaOperationService.authenticationCompleted(OPERATION_ID, OpTypeBO.CONSENT)).thenReturn(false);

        AuthConfirmationTO actual = middlewareUserService.verifyAuthConfirmationCode(AUTH_ID, AUTH_CONFIRM_CODE, USER_LOGIN);

        verify(scaOperationService, times(1)).verifyAuthConfirmationCode(anyString(), anyString());
        verify(scaOperationService, times(1)).authenticationCompleted(anyString(), any(OpTypeBO.class));
        verify(depositAccountPaymentService, times(0)).executePayment(anyString(), anyString());
        verify(depositAccountPaymentService, times(0)).updatePaymentStatus(anyString(), any(TransactionStatusBO.class));

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void verifyAuthConfirmationCode_Fail() throws Exception {
        AuthConfirmationTO expected = new AuthConfirmationTO()
                                              .success(false);

        whenNew(AuthConfirmationTO.class).withArguments(anyBoolean(), anyBoolean(), any(TransactionStatusTO.class), anyBoolean()).thenReturn(expected);

        when(scaOperationService.verifyAuthConfirmationCode(anyString(), anyString())).thenReturn(new ScaAuthConfirmationBO(false, any(OpTypeBO.class), anyString()));

        AuthConfirmationTO actual = middlewareUserService.verifyAuthConfirmationCode(AUTH_ID, AUTH_CONFIRM_CODE, USER_LOGIN);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void completeAuthConfirmation_Payment_Success() throws Exception {
        AuthConfirmationTO expected = new AuthConfirmationTO()
                                              .transactionStatus(TransactionStatusTO.ACCP)
                                              .success(true);

        whenNew(AuthConfirmationTO.class).withArguments(anyBoolean(), anyBoolean(), any(TransactionStatusTO.class), anyBoolean()).thenReturn(expected);

        when(scaOperationService.authenticationCompleted(OPERATION_ID, OpTypeBO.PAYMENT)).thenReturn(true);
        when(scaOperationService.completeAuthConfirmation(anyString(), anyBoolean())).thenReturn(new ScaAuthConfirmationBO(true, OpTypeBO.PAYMENT, OPERATION_ID));
        when(depositAccountPaymentService.executePayment(anyString(), anyString())).thenReturn(TransactionStatusBO.ACCP);

        AuthConfirmationTO actual = middlewareUserService.completeAuthConfirmation(AUTH_ID, true, USER_LOGIN);

        verify(scaOperationService, times(1)).authenticationCompleted(anyString(), any(OpTypeBO.class));
        verify(depositAccountPaymentService, times(1)).executePayment(anyString(), anyString());

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void completeAuthConfirmation_Payment_Fail() throws Exception {
        AuthConfirmationTO expected = new AuthConfirmationTO()
                                              .transactionStatus(TransactionStatusTO.RJCT)
                                              .success(false);

        whenNew(AuthConfirmationTO.class).withArguments(anyBoolean(), anyBoolean(), any(TransactionStatusTO.class), anyBoolean()).thenReturn(expected);

        when(scaOperationService.completeAuthConfirmation(anyString(), anyBoolean())).thenReturn(new ScaAuthConfirmationBO(false, OpTypeBO.PAYMENT, OPERATION_ID));
        when(depositAccountPaymentService.updatePaymentStatus(anyString(), any(TransactionStatusBO.class))).thenReturn(RJCT);

        AuthConfirmationTO actual = middlewareUserService.completeAuthConfirmation(AUTH_ID, false, USER_LOGIN);

        verify(scaOperationService, times(1)).completeAuthConfirmation(anyString(), anyBoolean());
        verify(scaOperationService, times(0)).authenticationCompleted(anyString(), any(OpTypeBO.class));
        verify(depositAccountPaymentService, times(1)).updatePaymentStatus(anyString(), any(TransactionStatusBO.class));
        verify(depositAccountPaymentService, times(0)).executePayment(anyString(), anyString());

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void completeAuthConfirmation_Consent_Fail() throws Exception {
        AuthConfirmationTO expected = new AuthConfirmationTO()
                                              .success(false);

        whenNew(AuthConfirmationTO.class).withArguments(anyBoolean(), anyBoolean(), any(TransactionStatusTO.class), anyBoolean()).thenReturn(expected);

        when(scaOperationService.completeAuthConfirmation(anyString(), anyBoolean())).thenReturn(new ScaAuthConfirmationBO(false, OpTypeBO.CONSENT, OPERATION_ID));

        AuthConfirmationTO actual = middlewareUserService.completeAuthConfirmation(AUTH_ID, false, USER_LOGIN);

        verify(scaOperationService, times(1)).completeAuthConfirmation(anyString(), anyBoolean());
        verify(scaOperationService, times(0)).authenticationCompleted(anyString(), any(OpTypeBO.class));
        verify(depositAccountPaymentService, times(0)).updatePaymentStatus(anyString(), any(TransactionStatusBO.class));
        verify(depositAccountPaymentService, times(0)).executePayment(anyString(), anyString());

        assertThat(actual).isEqualTo(expected);
    }
}