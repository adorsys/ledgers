package de.adorsys.ledgers.middleware.impl.service;

import de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO;
import de.adorsys.ledgers.deposit.api.service.DepositAccountPaymentService;
import de.adorsys.ledgers.middleware.api.domain.payment.TransactionStatusTO;
import de.adorsys.ledgers.middleware.api.domain.sca.AuthConfirmationTO;
import de.adorsys.ledgers.sca.domain.OpTypeBO;
import de.adorsys.ledgers.sca.domain.ScaAuthConfirmationBO;
import de.adorsys.ledgers.sca.service.SCAOperationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO.PATC;
import static de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO.RJCT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MiddlewareAuthConfirmationServiceTest {
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
    void verifyAuthConfirmationCode_Payment() {
        // Given
        AuthConfirmationTO expected = new AuthConfirmationTO()
                                              .success(true)
                                              .transactionStatus(TransactionStatusTO.ACCP);
        when(scaOperationService.verifyAuthConfirmationCode(anyString(), anyString())).thenReturn(new ScaAuthConfirmationBO(true, OpTypeBO.PAYMENT, OPERATION_ID));
        when(scaOperationService.authenticationCompleted(anyString(), any(OpTypeBO.class))).thenReturn(true);
        when(depositAccountPaymentService.executePayment(anyString(), anyString())).thenReturn(TransactionStatusBO.ACCP);

        // When
        AuthConfirmationTO actual = middlewareUserService.verifyAuthConfirmationCode(AUTH_ID, AUTH_CONFIRM_CODE, USER_LOGIN);

        // Then
        verify(scaOperationService, times(1)).verifyAuthConfirmationCode(anyString(), anyString());
        verify(scaOperationService, times(1)).authenticationCompleted(anyString(), any(OpTypeBO.class));
        verify(depositAccountPaymentService, times(1)).executePayment(anyString(), anyString());
        assertEquals(expected, actual);
    }

    @Test
    void verifyAuthConfirmationCode_Payment_Multilevel() {
        // Given
        ReflectionTestUtils.setField(middlewareUserService, "multilevelScaEnable", true);
        AuthConfirmationTO expected = new AuthConfirmationTO()
                                              .transactionStatus(TransactionStatusTO.PATC)
                                              .multilevelScaRequired(true)
                                              .partiallyAuthorised(true);

        when(scaOperationService.verifyAuthConfirmationCode(AUTH_ID, AUTH_CONFIRM_CODE)).thenReturn(new ScaAuthConfirmationBO(true, OpTypeBO.PAYMENT, OPERATION_ID));
        when(scaOperationService.authenticationCompleted(OPERATION_ID, OpTypeBO.PAYMENT)).thenReturn(false);
        when(depositAccountPaymentService.updatePaymentStatus(anyString(), any(TransactionStatusBO.class))).thenReturn(PATC);

        // When
        AuthConfirmationTO actual = middlewareUserService.verifyAuthConfirmationCode(AUTH_ID, AUTH_CONFIRM_CODE, USER_LOGIN);

        // Then
        verify(scaOperationService, times(1)).verifyAuthConfirmationCode(anyString(), anyString());
        verify(scaOperationService, times(1)).authenticationCompleted(anyString(), any(OpTypeBO.class));
        verify(depositAccountPaymentService, times(0)).executePayment(anyString(), anyString());
        verify(depositAccountPaymentService, times(1)).updatePaymentStatus(anyString(), any(TransactionStatusBO.class));

        assertEquals(expected.getTransactionStatus(), actual.getTransactionStatus());
    }

    @Test
    void verifyAuthConfirmationCode_Consent() {
        // Given
        AuthConfirmationTO expected = new AuthConfirmationTO()
                                              .success(true);

        when(scaOperationService.verifyAuthConfirmationCode(AUTH_ID, AUTH_CONFIRM_CODE)).thenReturn(new ScaAuthConfirmationBO(true, OpTypeBO.CONSENT, OPERATION_ID));
        when(scaOperationService.authenticationCompleted(OPERATION_ID, OpTypeBO.CONSENT)).thenReturn(true);

        // When
        AuthConfirmationTO actual = middlewareUserService.verifyAuthConfirmationCode(AUTH_ID, AUTH_CONFIRM_CODE, USER_LOGIN);

        // Then
        verify(scaOperationService, times(1)).verifyAuthConfirmationCode(anyString(), anyString());
        verify(scaOperationService, times(1)).authenticationCompleted(anyString(), any(OpTypeBO.class));
        verify(depositAccountPaymentService, times(0)).executePayment(anyString(), anyString());
        verify(depositAccountPaymentService, times(0)).updatePaymentStatus(anyString(), any(TransactionStatusBO.class));

        assertEquals(expected, actual);
    }

    @Test
    void verifyAuthConfirmationCode_Consent_Multilevel() {
        // Given
        ReflectionTestUtils.setField(middlewareUserService, "multilevelScaEnable", true);

        AuthConfirmationTO expected = new AuthConfirmationTO()
                                              .success(true)
                                              .partiallyAuthorised(true)
                                              .multilevelScaRequired(true);

        when(scaOperationService.verifyAuthConfirmationCode(AUTH_ID, AUTH_CONFIRM_CODE)).thenReturn(new ScaAuthConfirmationBO(true, OpTypeBO.CONSENT, OPERATION_ID));
        when(scaOperationService.authenticationCompleted(OPERATION_ID, OpTypeBO.CONSENT)).thenReturn(false);

        // When
        AuthConfirmationTO actual = middlewareUserService.verifyAuthConfirmationCode(AUTH_ID, AUTH_CONFIRM_CODE, USER_LOGIN);

        // Then
        verify(scaOperationService, times(1)).verifyAuthConfirmationCode(anyString(), anyString());
        verify(scaOperationService, times(1)).authenticationCompleted(anyString(), any(OpTypeBO.class));
        verify(depositAccountPaymentService, times(0)).executePayment(anyString(), anyString());
        verify(depositAccountPaymentService, times(0)).updatePaymentStatus(anyString(), any(TransactionStatusBO.class));

        assertEquals(expected, actual);
    }

    @Test
    void verifyAuthConfirmationCode_Fail() {
        // Given
        AuthConfirmationTO expected = new AuthConfirmationTO()
                                              .success(false);

        when(scaOperationService.verifyAuthConfirmationCode(anyString(), anyString())).thenReturn(new ScaAuthConfirmationBO(false, any(OpTypeBO.class), anyString()));

        // When
        AuthConfirmationTO actual = middlewareUserService.verifyAuthConfirmationCode(AUTH_ID, AUTH_CONFIRM_CODE, USER_LOGIN);

        // Then
        assertEquals(expected, actual);
    }

    @Test
    void completeAuthConfirmation_Payment_Success() {
        // Given
        AuthConfirmationTO expected = new AuthConfirmationTO()
                                              .transactionStatus(TransactionStatusTO.ACCP)
                                              .success(true);

        when(scaOperationService.authenticationCompleted(OPERATION_ID, OpTypeBO.PAYMENT)).thenReturn(true);
        when(scaOperationService.completeAuthConfirmation(anyString(), anyBoolean())).thenReturn(new ScaAuthConfirmationBO(true, OpTypeBO.PAYMENT, OPERATION_ID));
        when(depositAccountPaymentService.executePayment(anyString(), anyString())).thenReturn(TransactionStatusBO.ACCP);

        // When
        AuthConfirmationTO actual = middlewareUserService.completeAuthConfirmation(AUTH_ID, true, USER_LOGIN);

        // Then
        verify(scaOperationService, times(1)).authenticationCompleted(anyString(), any(OpTypeBO.class));
        verify(depositAccountPaymentService, times(1)).executePayment(anyString(), anyString());

        assertEquals(expected, actual);
    }

    @Test
    void completeAuthConfirmation_Payment_Fail() {
        // Given
        AuthConfirmationTO expected = new AuthConfirmationTO()
                                              .transactionStatus(TransactionStatusTO.RJCT)
                                              .success(false);

        when(scaOperationService.completeAuthConfirmation(anyString(), anyBoolean())).thenReturn(new ScaAuthConfirmationBO(false, OpTypeBO.PAYMENT, OPERATION_ID));
        when(depositAccountPaymentService.updatePaymentStatus(anyString(), any(TransactionStatusBO.class))).thenReturn(RJCT);

        // When
        AuthConfirmationTO actual = middlewareUserService.completeAuthConfirmation(AUTH_ID, false, USER_LOGIN);

        // Then
        verify(scaOperationService, times(1)).completeAuthConfirmation(anyString(), anyBoolean());
        verify(scaOperationService, times(0)).authenticationCompleted(anyString(), any(OpTypeBO.class));
        verify(depositAccountPaymentService, times(1)).updatePaymentStatus(anyString(), any(TransactionStatusBO.class));
        verify(depositAccountPaymentService, times(0)).executePayment(anyString(), anyString());

        assertEquals(expected, actual);
    }

    @Test
    void completeAuthConfirmation_Consent_Fail() {
        // Given
        AuthConfirmationTO expected = new AuthConfirmationTO()
                                              .success(false);

        when(scaOperationService.completeAuthConfirmation(anyString(), anyBoolean())).thenReturn(new ScaAuthConfirmationBO(false, OpTypeBO.CONSENT, OPERATION_ID));

        // When
        AuthConfirmationTO actual = middlewareUserService.completeAuthConfirmation(AUTH_ID, false, USER_LOGIN);

        // Then
        verify(scaOperationService, times(1)).completeAuthConfirmation(anyString(), anyBoolean());
        verify(scaOperationService, times(0)).authenticationCompleted(anyString(), any(OpTypeBO.class));
        verify(depositAccountPaymentService, times(0)).updatePaymentStatus(anyString(), any(TransactionStatusBO.class));
        verify(depositAccountPaymentService, times(0)).executePayment(anyString(), anyString());

        assertEquals(expected, actual);
    }
}