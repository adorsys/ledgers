package de.adorsys.ledgers.middleware.impl.service.message.step;

import de.adorsys.ledgers.middleware.api.domain.general.StepOperation;
import org.springframework.stereotype.Component;

@Component
public class SelectScaMethodStepMessageHandler extends BaseStepMessageHandler{
    @Override
    public String message(StepMessageHandlerRequest request) {
        return null;
    }

    @Override
    public StepOperation getStepOperation() {
        return StepOperation.SELECT_SCA_METHOD;
    }
}
