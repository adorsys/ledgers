/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.impl.service.message.step;

import de.adorsys.ledgers.deposit.api.domain.PaymentBO;
import de.adorsys.ledgers.deposit.api.domain.PaymentTypeBO;
import de.adorsys.ledgers.deposit.api.service.DepositAccountPaymentService;
import de.adorsys.ledgers.middleware.api.domain.general.StepOperation;
import de.adorsys.ledgers.sca.domain.OpTypeBO;
import de.adorsys.ledgers.sca.domain.SCAOperationBO;
import de.adorsys.ledgers.um.api.domain.AisAccountAccessInfoBO;
import de.adorsys.ledgers.um.api.domain.AisAccountAccessTypeBO;
import de.adorsys.ledgers.um.api.domain.AisConsentBO;
import de.adorsys.ledgers.um.api.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfirmationAuthCodeStepMessageHandlerTest {

    private static final String OP_ID = "12345";

    @Mock
    private DepositAccountPaymentService paymentService;
    @Mock
    private UserService userService;

    private ConfirmationAuthCodeStepMessageHandler handler;
    private SCAOperationBO scaOperation;

    @BeforeEach
    void setUp() {
        handler = new ConfirmationAuthCodeStepMessageHandler(paymentService, userService);

        scaOperation = new SCAOperationBO();
        scaOperation.setOpId(OP_ID);
    }

    @Test
    void getStepOperation() {
        assertEquals(StepOperation.CONFIRM_AUTH_CODE, handler.getStepOperation());
    }

    @Test
    void message_consent() {
        scaOperation.setOpType(OpTypeBO.CONSENT);

        AisConsentBO operationObject = new AisConsentBO();
        operationObject.setTppId("tpp-id");
        AisAccountAccessInfoBO access = new AisAccountAccessInfoBO();
        access.setAllPsd2(AisAccountAccessTypeBO.ALL_ACCOUNTS);
        access.setAccounts(List.of("DE91100000000123456789"));
        operationObject.setAccess(access);
        when(userService.loadConsent(OP_ID)).thenReturn(operationObject);

        String message = handler.message(StepMessageHandlerRequest.builder()
                                                 .scaOperation(scaOperation)
                                                 .build());

        assertEquals("Your Login for CONSENT id: 12345 is successful", message);
    }

    @Test
    void message_payment() {
        scaOperation.setOpType(OpTypeBO.PAYMENT);
        PaymentBO operationObject = new PaymentBO();
        operationObject.setPaymentId(OP_ID);
        operationObject.setPaymentType(PaymentTypeBO.SINGLE);

        when(paymentService.getPaymentById(OP_ID)).thenReturn(operationObject);

        String message = handler.message(StepMessageHandlerRequest.builder()
                                                 .scaOperation(scaOperation)
                                                 .build());

        assertEquals("Your Login for PAYMENT id: 12345 is successful", message);
    }
}