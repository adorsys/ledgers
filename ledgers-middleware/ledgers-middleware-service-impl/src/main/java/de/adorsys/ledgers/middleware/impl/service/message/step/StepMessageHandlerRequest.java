package de.adorsys.ledgers.middleware.impl.service.message.step;

import de.adorsys.ledgers.sca.domain.OpTypeBO;
import de.adorsys.ledgers.sca.domain.SCAOperationBO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StepMessageHandlerRequest {
    private OpTypeBO opType;
    private SCAOperationBO scaOperation;
    private Object operationObject;
    private boolean isScaRequired;
}
