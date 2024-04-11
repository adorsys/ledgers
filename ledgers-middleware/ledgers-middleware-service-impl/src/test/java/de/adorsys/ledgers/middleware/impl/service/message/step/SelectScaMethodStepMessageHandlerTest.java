/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.impl.service.message.step;

import de.adorsys.ledgers.middleware.api.domain.general.StepOperation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SelectScaMethodStepMessageHandlerTest {
    private SelectScaMethodStepMessageHandler handler;

    @BeforeEach
    void setUp() {
        handler = new SelectScaMethodStepMessageHandler();
    }

    @Test
    void getStepOperation() {
        assertEquals(StepOperation.SELECT_SCA_METHOD, handler.getStepOperation());
    }

    @Test
    void message() {
        String message = handler.message(StepMessageHandlerRequest.builder()
                                                 .build());
        assertNull(message);
    }
}