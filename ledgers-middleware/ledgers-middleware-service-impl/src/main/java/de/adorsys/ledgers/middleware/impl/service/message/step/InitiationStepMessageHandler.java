/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.impl.service.message.step;

import de.adorsys.ledgers.middleware.api.domain.general.StepOperation;
import org.springframework.stereotype.Component;

@Component
public class InitiationStepMessageHandler extends BaseStepMessageHandler {
    @Override
    public String message(StepMessageHandlerRequest request) {
        return null;
    }

    @Override
    public StepOperation getStepOperation() {
        return StepOperation.INITIATION;
    }
}
