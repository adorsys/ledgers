/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.impl.service.message;

import de.adorsys.ledgers.middleware.api.domain.general.StepOperation;
import de.adorsys.ledgers.middleware.impl.service.message.step.StepMessageHandler;
import de.adorsys.ledgers.middleware.impl.service.message.step.StepMessageHandlerRequest;
import de.adorsys.ledgers.sca.domain.OpTypeBO;
import de.adorsys.ledgers.sca.domain.SCAOperationBO;
import de.adorsys.ledgers.um.api.domain.AisConsentBO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PsuMessageResolverImplTest {

    private PsuMessageResolverImpl psuMessageResolver;

    @Mock
    private StepMessageHandler stepMessageHandler;

    @Captor
    private ArgumentCaptor<StepMessageHandlerRequest> stepMessageHandlerRequest;
    private SCAOperationBO operation;

    @BeforeEach
    void setUp() {
        psuMessageResolver = new PsuMessageResolverImpl(List.of(stepMessageHandler));

        operation = new SCAOperationBO();
        operation.setOpType(OpTypeBO.CONSENT);
    }

    @Test
    void getMessageHandler_noStepOperationError() {
        when(stepMessageHandler.getStepOperation()).thenReturn(StepOperation.START_SCA);
        assertThrows(IllegalArgumentException.class, () -> psuMessageResolver.getMessageHandler(null));
    }

    @Test
    void messageWithOperation() {
        when(stepMessageHandler.getStepOperation()).thenReturn(StepOperation.START_SCA);

        psuMessageResolver.message(StepOperation.START_SCA, operation);

        verify(stepMessageHandler).message(stepMessageHandlerRequest.capture());

        assertEquals(operation, stepMessageHandlerRequest.getValue().getScaOperation());
        assertEquals(OpTypeBO.CONSENT, stepMessageHandlerRequest.getValue().getOpType());
        assertNull(stepMessageHandlerRequest.getValue().getOperationObject());
        assertFalse(stepMessageHandlerRequest.getValue().isScaRequired());
    }

    @Test
    void messageWithOperationAndOperationObject() {
        AisConsentBO operationObject = new AisConsentBO();
        when(stepMessageHandler.getStepOperation()).thenReturn(StepOperation.START_SCA);

        psuMessageResolver.message(StepOperation.START_SCA, operation, operationObject);

        verify(stepMessageHandler).message(stepMessageHandlerRequest.capture());

        assertEquals(operation, stepMessageHandlerRequest.getValue().getScaOperation());
        assertEquals(OpTypeBO.CONSENT, stepMessageHandlerRequest.getValue().getOpType());
        assertEquals(operationObject, stepMessageHandlerRequest.getValue().getOperationObject());
        assertFalse(stepMessageHandlerRequest.getValue().isScaRequired());
    }

    @Test
    void messageWithOpTypeAndScaRequiredAndOperationObject() {
        AisConsentBO operationObject = new AisConsentBO();
        when(stepMessageHandler.getStepOperation()).thenReturn(StepOperation.START_SCA);

        psuMessageResolver.message(StepOperation.START_SCA, OpTypeBO.CONSENT, true, operationObject);

        verify(stepMessageHandler).message(stepMessageHandlerRequest.capture());

        assertNull(stepMessageHandlerRequest.getValue().getScaOperation());
        assertEquals(OpTypeBO.CONSENT, stepMessageHandlerRequest.getValue().getOpType());
        assertEquals(operationObject, stepMessageHandlerRequest.getValue().getOperationObject());
        assertTrue(stepMessageHandlerRequest.getValue().isScaRequired());
    }
}