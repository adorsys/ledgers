package de.adorsys.ledgers.middleware.impl.service.message.step;

import de.adorsys.ledgers.middleware.api.domain.general.StepOperation;
import org.springframework.stereotype.Component;

@Component
public class StartScaStepMessageHandler extends BaseStepMessageHandler {
    @Override
    public String message(StepMessageHandlerRequest request) {
        return updateMessage("", request);
    }

    @Override
    public StepOperation getStepOperation() {
        return StepOperation.START_SCA;
    }

}
