package de.adorsys.ledgers.middleware.impl.service.validation;

import de.adorsys.ledgers.deposit.api.domain.PaymentBO;
import de.adorsys.ledgers.deposit.api.domain.PaymentTargetBO;
import de.adorsys.ledgers.deposit.api.domain.PaymentTypeBO;
import de.adorsys.ledgers.deposit.api.service.DepositAccountPaymentService;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareErrorCode;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareModuleException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentIdValidatorTest {

    private static final String NEW_ID = "newId";
    private static final String EXISTING_ID = "existingId";

    @InjectMocks
    private PaymentIdValidator service;

    @Mock
    private DepositAccountPaymentService paymentService;

    @Test
    void validateIds_no_ids_set() {
        when(paymentService.existingPaymentById(any())).thenReturn(false);
        when(paymentService.existingTargetById(any())).thenReturn(false);
        PaymentBO payment = getPayment(null, null, null);
        service.check(payment, null);
        assertNotNull(payment.getPaymentId());
        assertNotNull(payment.getTargets().get(0).getPaymentId());
        assertNotNull(payment.getTargets().get(1).getPaymentId());
        verify(paymentService, times(1)).existingPaymentById(any());
        verify(paymentService, times(2)).existingTargetById(any());
    }

    @Test
    void validateIds_all_new_ids() {
        when(paymentService.existingPaymentById(any())).thenReturn(false);
        when(paymentService.existingTargetById(any())).thenReturn(false);
        PaymentBO payment = getPayment(NEW_ID, NEW_ID, NEW_ID);
        service.check(payment, null);
        assertEquals(NEW_ID, payment.getPaymentId());
        assertEquals(NEW_ID, payment.getTargets().get(0).getPaymentId());
        assertEquals(NEW_ID, payment.getTargets().get(1).getPaymentId());
        verify(paymentService, times(1)).existingPaymentById(any());
        verify(paymentService, times(2)).existingTargetById(any());
    }

    @Test
    void validateIds_second_trg_id_exists() {
        when(paymentService.existingPaymentById(any())).thenReturn(false);
        when(paymentService.existingTargetById(eq(NEW_ID))).thenReturn(false);
        when(paymentService.existingTargetById(eq(EXISTING_ID))).thenReturn(true);
        PaymentBO payment = getPayment(NEW_ID, NEW_ID, EXISTING_ID);
        service.check(payment, null);
        assertEquals(NEW_ID, payment.getPaymentId());
        assertEquals(NEW_ID, payment.getTargets().get(0).getPaymentId());
        assertNotNull(payment.getTargets().get(1).getPaymentId());
        assertNotEquals(NEW_ID, payment.getTargets().get(1).getPaymentId());
        assertNotEquals(EXISTING_ID, payment.getTargets().get(1).getPaymentId());
        verify(paymentService, times(1)).existingPaymentById(any());
        verify(paymentService, times(3)).existingTargetById(any());
    }

    @Test
    void validateIds_pmt_id_exists() {
        when(paymentService.existingPaymentById(eq(EXISTING_ID))).thenReturn(true);
        PaymentBO payment = getPayment(EXISTING_ID, NEW_ID, NEW_ID);
        MiddlewareModuleException exception = assertThrows(MiddlewareModuleException.class, () -> service.check(payment, null));
        assertEquals(MiddlewareErrorCode.PAYMENT_VALIDATION_EXCEPTION, exception.getErrorCode());

        verify(paymentService, times(1)).existingPaymentById(any());
    }

    private PaymentBO getPayment(String pmtId, String trg1Id, String trg2Id) {
        return new PaymentBO(pmtId, false, null, null,
                             PaymentTypeBO.BULK, null, null, null, null,
                             null, null, null, null, null,
                             null, List.of(getTarget(trg1Id), getTarget(trg2Id)), null);
    }

    private PaymentTargetBO getTarget(String targetId) {
        return new PaymentTargetBO(targetId, null, null, null, null,
                                   null, null, null, null,
                                   null, null, null);
    }

}