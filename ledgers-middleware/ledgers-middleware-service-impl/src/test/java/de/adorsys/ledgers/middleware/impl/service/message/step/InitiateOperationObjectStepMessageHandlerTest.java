/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.impl.service.message.step;

import de.adorsys.ledgers.deposit.api.domain.PaymentBO;
import de.adorsys.ledgers.deposit.api.domain.PaymentTypeBO;
import de.adorsys.ledgers.middleware.api.domain.general.StepOperation;
import de.adorsys.ledgers.sca.domain.OpTypeBO;
import de.adorsys.ledgers.sca.domain.SCAOperationBO;
import de.adorsys.ledgers.um.api.domain.AisAccountAccessInfoBO;
import de.adorsys.ledgers.um.api.domain.AisAccountAccessTypeBO;
import de.adorsys.ledgers.um.api.domain.AisConsentBO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InitiateOperationObjectStepMessageHandlerTest {

    private static final String OP_ID = "12345";

    private InitiateOperationObjectStepMessageHandler handler;
    private SCAOperationBO scaOperation;

    @BeforeEach
    void setUp() {
        handler = new InitiateOperationObjectStepMessageHandler();

        scaOperation = new SCAOperationBO();
        scaOperation.setOpId(OP_ID);
    }

    @Test
    void getStepOperation() {
        assertEquals(StepOperation.INITIATE_OPERATION_OBJECT, handler.getStepOperation());
    }

    @Test
    void message_consent() {
        AisConsentBO operationObject = new AisConsentBO();
        operationObject.setTppId("tpp-id");
        AisAccountAccessInfoBO access = new AisAccountAccessInfoBO();
        access.setAllPsd2(AisAccountAccessTypeBO.ALL_ACCOUNTS);
        access.setAccounts(List.of("DE91100000000123456789"));
        operationObject.setAccess(access);

        String message = handler.message(StepMessageHandlerRequest.builder()
                                                 .scaOperation(scaOperation)
                                                 .opType(OpTypeBO.CONSENT)
                                                 .operationObject(operationObject)
                                                 .build());
        assertEquals("Account access for TPP with id tpp-id:\n" +
                             "- For one time access.\n" +
                             "Access to following accounts:\n" +
                             "All payments accounts without balances.\n" +
                             "Without balances: DE91100000000123456789.\n", message);
    }

    @Test
    void message_payment() {
        PaymentBO operationObject = new PaymentBO();
        operationObject.setPaymentId(OP_ID);
        operationObject.setPaymentType(PaymentTypeBO.SINGLE);

        String message = handler.message(StepMessageHandlerRequest.builder()
                                                 .scaOperation(scaOperation)
                                                 .opType(OpTypeBO.PAYMENT)
                                                 .operationObject(operationObject)
                                                 .build());

        assertEquals("Your SINGLE Payment order # 12345 is scheduled", message);
    }

    @Test
    void message_piisConsent() {
        AisConsentBO operationObject = new AisConsentBO();
        operationObject.setTppId("tpp-id");
        AisAccountAccessInfoBO access = new AisAccountAccessInfoBO();
        access.setAllPsd2(AisAccountAccessTypeBO.ALL_ACCOUNTS);
        access.setAccounts(List.of("DE91100000000123456789"));
        operationObject.setAccess(access);

        String message = handler.message(StepMessageHandlerRequest.builder()
                                                 .scaOperation(scaOperation)
                                                 .opType(OpTypeBO.PIIS_CONSENT)
                                                 .operationObject(operationObject)
                                                 .build());
        assertEquals("Account access for TPP with id tpp-id:\n" +
                             "- For one time access.\n" +
                             "Access to following accounts:\n" +
                             "All payments accounts without balances.\n" +
                             "Without balances: DE91100000000123456789.\n" +
                             "This access has been granted. No TAN entry needed.", message);
    }
}