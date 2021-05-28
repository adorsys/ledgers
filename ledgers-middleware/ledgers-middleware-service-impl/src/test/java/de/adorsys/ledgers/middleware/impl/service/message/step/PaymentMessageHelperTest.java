package de.adorsys.ledgers.middleware.impl.service.message.step;

import de.adorsys.ledgers.deposit.api.domain.PaymentTypeBO;
import de.adorsys.ledgers.sca.domain.OpTypeBO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PaymentMessageHelperTest {

    private static final String PAYMENT_ID = "12345678";

    private PaymentMessageHelper helper;

    @Test
    void getTanTemplate_payment() {
        helper = new PaymentMessageHelper(PAYMENT_ID, OpTypeBO.PAYMENT, PaymentTypeBO.SINGLE);
        String message = helper.getTanTemplate();
        assertEquals("The TAN for your SINGLE Payment order # 12345678 is: %s", message);
    }

    @Test
    void getTanTemplate_cancelPayment() {
        helper = new PaymentMessageHelper(PAYMENT_ID, OpTypeBO.CANCEL_PAYMENT, PaymentTypeBO.SINGLE);
        String message = helper.getTanTemplate();
        assertEquals("The TAN for your  Payment Cancellation order # 12345678 is: %s", message);
    }

    @Test
    void getExemptedTemplate_payment() {
        helper = new PaymentMessageHelper(PAYMENT_ID, OpTypeBO.PAYMENT, PaymentTypeBO.SINGLE);
        String message = helper.getExemptedTemplate();
        assertEquals("Your SINGLE Payment order # 12345678 is scheduled", message);
    }

    @Test
    void getExemptedTemplate_cancelPayment() {
        helper = new PaymentMessageHelper(PAYMENT_ID, OpTypeBO.CANCEL_PAYMENT, PaymentTypeBO.SINGLE);
        String message = helper.getExemptedTemplate();
        assertEquals("Your  Payment Cancellation order # 12345678 is scheduled", message);
    }

    @Test
    void resolveMessage_payment() {
        helper = new PaymentMessageHelper(PAYMENT_ID, OpTypeBO.PAYMENT, PaymentTypeBO.SINGLE);
        String message = helper.resolveMessage(true);
        assertEquals("The TAN for your SINGLE Payment order # 12345678 is: %s", message);

        message = helper.resolveMessage(false);
        assertEquals("Your SINGLE Payment order # 12345678 is scheduled", message);
    }

    @Test
    void resolveMessage_cancelPayment() {
        helper = new PaymentMessageHelper(PAYMENT_ID, OpTypeBO.CANCEL_PAYMENT, PaymentTypeBO.SINGLE);
        String message = helper.resolveMessage(true);
        assertEquals("The TAN for your  Payment Cancellation order # 12345678 is: %s", message);

        message = helper.resolveMessage(false);
        assertEquals("Your  Payment Cancellation order # 12345678 is scheduled", message);
    }
}