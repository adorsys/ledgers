package de.adorsys.ledgers.middleware.impl.service.message.step;

import de.adorsys.ledgers.middleware.api.domain.general.StepOperation;
import de.adorsys.ledgers.sca.domain.OpTypeBO;
import de.adorsys.ledgers.sca.domain.SCAOperationBO;
import de.adorsys.ledgers.sca.domain.ScaStatusBO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GetScaMethodsStepMessageHandlerTest {

    private static final String OP_ID = "12345";

    private GetScaMethodsStepMessageHandler handler;
    private SCAOperationBO scaOperation;

    private static Stream<Arguments> scaStatuses() {
        return Stream.of(
                Arrays.stream(ScaStatusBO.values())
                        .filter(status -> !EnumSet.of(ScaStatusBO.SCAMETHODSELECTED, ScaStatusBO.FINALISED).contains(status))
                        .map(Arguments::arguments)
                        .toArray(Arguments[]::new)
        );
    }

    @BeforeEach
    void setUp() {
        handler = new GetScaMethodsStepMessageHandler();

        scaOperation = new SCAOperationBO();
        scaOperation.setOpId(OP_ID);
        scaOperation.setOpType(OpTypeBO.CONSENT);
    }

    @Test
    void getStepOperation() {
        assertEquals(StepOperation.GET_SCA_METHODS, handler.getStepOperation());
    }

    @Test
    void message_scaStatusIsFinalised() {
        scaOperation.setScaStatus(ScaStatusBO.FINALISED);

        String message = handler.message(StepMessageHandlerRequest.builder()
                                                 .scaOperation(scaOperation)
                                                 .build());

        assertEquals("Your CONSENT id: 12345 is confirmed", message);
    }

    @Test
    void message_scaStatusIsScaMethodSelected() {
        scaOperation.setScaStatus(ScaStatusBO.SCAMETHODSELECTED);

        String message = handler.message(StepMessageHandlerRequest.builder()
                                                 .scaOperation(scaOperation)
                                                 .build());

        assertEquals("", message);
    }

    @ParameterizedTest
    @MethodSource("scaStatuses")
    void message_scaStatusIsAnother(ScaStatusBO scaStatus) {
        scaOperation.setScaStatus(scaStatus);

        String message = handler.message(StepMessageHandlerRequest.builder()
                                                 .scaOperation(scaOperation)
                                                 .build());

        assertEquals("Your Login for CONSENT id: 12345 is successful", message);
    }
}