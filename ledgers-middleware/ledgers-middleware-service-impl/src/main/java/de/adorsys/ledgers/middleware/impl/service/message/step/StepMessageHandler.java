package de.adorsys.ledgers.middleware.impl.service.message.step;

import de.adorsys.ledgers.middleware.api.domain.general.StepOperation;

public interface StepMessageHandler {

    String message(StepMessageHandlerRequest request);

    StepOperation getStepOperation();
}
