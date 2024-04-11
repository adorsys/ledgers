/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.impl.service.message.step;

import de.adorsys.ledgers.deposit.api.domain.PaymentBO;
import de.adorsys.ledgers.middleware.api.domain.general.StepOperation;
import de.adorsys.ledgers.sca.domain.OpTypeBO;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

import static de.adorsys.ledgers.sca.domain.OpTypeBO.CANCEL_PAYMENT;
import static de.adorsys.ledgers.sca.domain.OpTypeBO.PAYMENT;

@Component
public class AuthorizeStepMessageHandler implements StepMessageHandler {
    @Override
    public String message(StepMessageHandlerRequest request) {
        OpTypeBO opType = request.getOpType();
        if (EnumSet.of(PAYMENT, CANCEL_PAYMENT).contains(opType)) {
            PaymentBO payment = (PaymentBO) request.getOperationObject();
            return new PaymentMessageHelper(payment.getPaymentId(), opType, payment.getPaymentType()).resolveMessage(request.isScaRequired());
        }
        return null;
    }

    @Override
    public StepOperation getStepOperation() {
        return StepOperation.AUTHORIZE;
    }
}
