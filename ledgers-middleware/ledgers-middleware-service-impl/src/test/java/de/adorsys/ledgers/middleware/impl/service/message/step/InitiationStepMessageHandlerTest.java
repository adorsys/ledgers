package de.adorsys.ledgers.middleware.impl.service.message.step;

import de.adorsys.ledgers.middleware.api.domain.general.StepOperation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class InitiationStepMessageHandlerTest {
    private InitiationStepMessageHandler handler;

    @BeforeEach
    void setUp() {
        handler = new InitiationStepMessageHandler();
    }

    @Test
    void getStepOperation() {
        assertEquals(StepOperation.INITIATION, handler.getStepOperation());
    }

    @Test
    void message() {
        String message = handler.message(StepMessageHandlerRequest.builder()
                                                 .build());
        assertNull(message);
    }
}