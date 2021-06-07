package de.adorsys.ledgers.middleware.impl.service.message;

import de.adorsys.ledgers.middleware.api.domain.general.StepOperation;
import de.adorsys.ledgers.sca.domain.OpTypeBO;
import de.adorsys.ledgers.sca.domain.SCAOperationBO;

public interface PsuMessageResolver {

    String message(StepOperation stepOperation, SCAOperationBO operation);

    String message(StepOperation stepOperation, SCAOperationBO operation, Object operationObject);

    String message(StepOperation stepOperation, OpTypeBO opType, boolean isScaRequired, Object operationObject);
}
