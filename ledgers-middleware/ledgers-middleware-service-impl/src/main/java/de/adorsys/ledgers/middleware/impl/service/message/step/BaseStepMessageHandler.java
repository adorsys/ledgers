/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.impl.service.message.step;

import de.adorsys.ledgers.sca.domain.SCAOperationBO;

import static de.adorsys.ledgers.sca.domain.ScaStatusBO.FINALISED;
import static de.adorsys.ledgers.sca.domain.ScaStatusBO.SCAMETHODSELECTED;

public abstract class BaseStepMessageHandler implements StepMessageHandler {

    protected String updateMessage(String template, StepMessageHandlerRequest request) {
        SCAOperationBO scaOperation = request.getScaOperation();
        if (SCAMETHODSELECTED.equals(scaOperation.getScaStatus())) {
            return String.format(template, scaOperation.getTan());
        } else if (FINALISED.equals(scaOperation.getScaStatus())) {
            return String.format("Your %s id: %s is confirmed", scaOperation.getOpType(), scaOperation.getOpId());
        } else {
            return String.format("Your Login for %s id: %s is successful", scaOperation.getOpType(), scaOperation.getOpId());
        }
    }
}
