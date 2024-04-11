/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.impl.service.message.step;

import de.adorsys.ledgers.deposit.api.domain.PaymentBO;
import de.adorsys.ledgers.deposit.api.domain.PaymentTypeBO;
import de.adorsys.ledgers.middleware.api.domain.general.StepOperation;
import de.adorsys.ledgers.sca.domain.OpTypeBO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AuthorizeStepMessageHandlerTest {

    private AuthorizeStepMessageHandler handler;
    private PaymentBO operationObject;

    @BeforeEach
    void setUp() {
        handler = new AuthorizeStepMessageHandler();

        operationObject = new PaymentBO();
        operationObject.setPaymentId("111");
        operationObject.setPaymentType(PaymentTypeBO.SINGLE);
    }

    @Test
    void getStepOperation() {
        assertEquals(StepOperation.AUTHORIZE, handler.getStepOperation());
    }

    @Test
    void message_singlePayment() {
        String message = handler.message(StepMessageHandlerRequest.builder()
                                                 .opType(OpTypeBO.PAYMENT)
                                                 .isScaRequired(false)
                                                 .operationObject(operationObject)
                                                 .build());
        assertEquals("Your SINGLE Payment order # 111 is scheduled", message);
    }

    @Test
    void message_cancelPayment() {
        String message = handler.message(StepMessageHandlerRequest.builder()
                                                 .opType(OpTypeBO.CANCEL_PAYMENT)
                                                 .isScaRequired(true)
                                                 .operationObject(operationObject)
                                                 .build());
        assertEquals("The TAN for your  Payment Cancellation order # 111 is: %s", message);
    }

    @Test
    void message_consent() {
        String message = handler.message(StepMessageHandlerRequest.builder()
                                                 .opType(OpTypeBO.CONSENT)
                                                 .build());
        assertNull(message);
    }
}