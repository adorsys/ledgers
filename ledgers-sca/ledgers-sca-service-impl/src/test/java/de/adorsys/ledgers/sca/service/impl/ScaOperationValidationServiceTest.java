package de.adorsys.ledgers.sca.service.impl;

import de.adorsys.ledgers.sca.db.domain.SCAOperationEntity;
import de.adorsys.ledgers.sca.db.domain.ScaStatus;
import de.adorsys.ledgers.sca.db.repository.SCAOperationRepository;
import de.adorsys.ledgers.sca.domain.AuthCodeDataBO;
import de.adorsys.ledgers.sca.domain.OpTypeBO;
import de.adorsys.ledgers.um.api.domain.ScaUserDataBO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.util.exception.ScaModuleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.FieldSetter;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;

import static de.adorsys.ledgers.util.exception.SCAErrorCode.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ScaOperationValidationServiceTest {
    @InjectMocks
    private ScaOperationValidationService service;
    @Mock
    private SCAOperationRepository repository;

    @BeforeEach
    void prepare() throws NoSuchFieldException {
        FieldSetter.setField(service, service.getClass().getDeclaredField("finalWeight"), 100);
        FieldSetter.setField(service, service.getClass().getDeclaredField("authCodeFailedMax"), 5);
        FieldSetter.setField(service, service.getClass().getDeclaredField("loginFailedMax"), 3);
    }

    @Test
    void checkValidityAndAttempts() {
        SCAOperationEntity operation = new SCAOperationEntity(null, null, null, null, "scaId", 600, 600, null, 0);
        UserBO user = new UserBO();
        user.setScaUserData(Collections.singletonList(new ScaUserDataBO()));
        AuthCodeDataBO data = new AuthCodeDataBO();
        assertDoesNotThrow(() -> service.checkValidityAndAttempts(operation, data, user));
    }

    @Test
    void checkValidityAndAttempts_fail_attempts() {
        SCAOperationEntity operation = new SCAOperationEntity(null, null, null, null, "scaId", 600, 600, null, 0);
        operation.setFailledCount(5);
        UserBO user = new UserBO();
        user.setScaUserData(Collections.singletonList(new ScaUserDataBO()));
        AuthCodeDataBO data = new AuthCodeDataBO();
        ScaModuleException exception = assertThrows(ScaModuleException.class, () -> service.checkValidityAndAttempts(operation, data, user));
        assertSame(SCA_OPERATION_FAILED, exception.getErrorCode());
    }

    @Test
    void checkValidityAndAttempts_fail_data_invalid() {
        SCAOperationEntity operation = new SCAOperationEntity(null, null, null, null, null, 600, 600, null, 0);
        UserBO user = new UserBO();
        user.setScaUserData(Collections.singletonList(new ScaUserDataBO()));
        AuthCodeDataBO data = new AuthCodeDataBO();
        ScaModuleException exception = assertThrows(ScaModuleException.class, () -> service.checkValidityAndAttempts(operation, data, user));
        assertSame(SCA_OPERATION_VALIDATION_INVALID, exception.getErrorCode());
    }

    @Test
    void checkValidityAndAttempts_fail_user_has_no_sca() {
        SCAOperationEntity operation = new SCAOperationEntity(null, null, null, null, "scaId", 600, 600, null, 0);
        UserBO user = new UserBO();
        AuthCodeDataBO data = new AuthCodeDataBO();
        ScaModuleException exception = assertThrows(ScaModuleException.class, () -> service.checkValidityAndAttempts(operation, data, user));
        assertSame(SCA_OPERATION_VALIDATION_INVALID, exception.getErrorCode());
    }

    @Test
    void checkAll() {
        SCAOperationEntity operation = new SCAOperationEntity(null, "opId", null, null, "scaId", 600, 600, null, 0);
        assertDoesNotThrow(() -> service.checkAll(operation, "opId"));
    }

    @Test
    void checkAll_fail_op_failed() {
        SCAOperationEntity operation = new SCAOperationEntity(null, "opId", null, null, "scaId", 600, 600, ScaStatus.FAILED, 0);
        ScaModuleException exception = assertThrows(ScaModuleException.class, () -> service.checkAll(operation, "opId"));
        assertSame(SCA_OPERATION_USED_OR_STOLEN, exception.getErrorCode());
    }

    @Test
    void checkAll_fail_op_expired() {
        SCAOperationEntity operation = new SCAOperationEntity(null, "opId", null, null, "scaId", 600, 600, null, 0);
        operation.setCreated(LocalDateTime.now().minusDays(1));
        ScaModuleException exception = assertThrows(ScaModuleException.class, () -> service.checkAll(operation, "opId"));
        assertSame(SCA_OPERATION_EXPIRED, exception.getErrorCode());
    }

    @Test
    void checkAll_fail_op_is_different() {
        SCAOperationEntity operation = new SCAOperationEntity(null, "opId", null, null, "scaId", 600, 600, null, 0);
        ScaModuleException exception = assertThrows(ScaModuleException.class, () -> service.checkAll(operation, "anotherId"));
        assertSame(SCA_OPERATION_VALIDATION_INVALID, exception.getErrorCode());
    }

    @Test
    void checksOnPresentOperation() {
        SCAOperationEntity operation = new SCAOperationEntity(null, "opId", null, null, "scaId", 600, 600, null, 0);
        assertDoesNotThrow(() -> service.checksOnPresentOperation(operation));
    }

    @Test
    void isMultiLevelScaCompleted() {
        SCAOperationEntity operation = new SCAOperationEntity(null, "opId", null, null, "scaId", 600, 600, ScaStatus.FINALISED, 100);
        boolean result = service.isMultiLevelScaCompleted(Collections.singletonList(operation), OpTypeBO.PAYMENT);
        assertTrue(result);
    }

    @Test
    void isMultiLevelScaCompleted_failed_weight() {
        SCAOperationEntity operation = new SCAOperationEntity(null, "opId", null, null, "scaId", 600, 600, ScaStatus.FINALISED, 90);
        boolean result = service.isMultiLevelScaCompleted(Collections.singletonList(operation), OpTypeBO.PAYMENT);
        assertFalse(result);
    }

    @Test
    void isMultiLevelScaCompleted_failed_sca_status() {
        SCAOperationEntity operation = new SCAOperationEntity(null, "opId", null, null, "scaId", 600, 600, ScaStatus.PSUAUTHENTICATED, 100);
        boolean result = service.isMultiLevelScaCompleted(Collections.singletonList(operation), OpTypeBO.PAYMENT);
        assertFalse(result);
    }

    @Test
    void isMultiLevelScaCompleted_failed_login_op() {
        SCAOperationEntity operation = new SCAOperationEntity(null, "opId", null, null, "scaId", 600, 600, ScaStatus.FINALISED, 100);
        boolean result = service.isMultiLevelScaCompleted(Collections.singletonList(operation), OpTypeBO.LOGIN);
        assertFalse(result);
    }

    @Test
    void isAnyScaCompleted() {
        SCAOperationEntity operation = new SCAOperationEntity(null, "opId", null, null, "scaId", 600, 600, ScaStatus.FINALISED, 0);
        boolean result = service.isAnyScaCompleted(Collections.singletonList(operation));
        assertTrue(result);
    }

    @Test
    void isAnyScaCompleted_failed() {
        SCAOperationEntity operation = new SCAOperationEntity(null, "opId", null, null, "scaId", 600, 600, ScaStatus.SCAMETHODSELECTED, 0);
        boolean result = service.isAnyScaCompleted(Collections.singletonList(operation));
        assertFalse(result);
    }
}