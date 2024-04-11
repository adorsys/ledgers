/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.impl.service.message;

import de.adorsys.ledgers.middleware.api.domain.general.StepOperation;
import de.adorsys.ledgers.middleware.impl.service.message.step.StepMessageHandler;
import de.adorsys.ledgers.middleware.impl.service.message.step.StepMessageHandlerRequest;
import de.adorsys.ledgers.sca.domain.OpTypeBO;
import de.adorsys.ledgers.sca.domain.SCAOperationBO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PsuMessageResolverImpl implements PsuMessageResolver {

    private final List<StepMessageHandler> stepMessageHandlers;

    @Override
    public String message(StepOperation stepOperation, SCAOperationBO operation) {
        return getMessageHandler(stepOperation).message(StepMessageHandlerRequest.builder()
                                                                .scaOperation(operation)
                                                                .opType(operation.getOpType())
                                                                .build());
    }

    @Override
    public String message(StepOperation stepOperation, SCAOperationBO operation, Object operationObject) {
        return getMessageHandler(stepOperation).message(StepMessageHandlerRequest.builder()
                                                                .scaOperation(operation)
                                                                .opType(operation.getOpType())
                                                                .operationObject(operationObject)
                                                                .build());
    }

    @Override
    public String message(StepOperation stepOperation, OpTypeBO opType, boolean isScaRequired, Object operationObject) {
        return getMessageHandler(stepOperation).message(StepMessageHandlerRequest.builder()
                                                                .opType(opType)
                                                                .isScaRequired(isScaRequired)
                                                                .operationObject(operationObject)
                                                                .build());
    }

    StepMessageHandler getMessageHandler(StepOperation stepOperation) {
        return stepMessageHandlers.stream().filter(so -> so.getStepOperation() == stepOperation)
                       .findFirst()
                       .orElseThrow(() -> new IllegalArgumentException(stepOperation + " step operation not supported."));
    }
}
