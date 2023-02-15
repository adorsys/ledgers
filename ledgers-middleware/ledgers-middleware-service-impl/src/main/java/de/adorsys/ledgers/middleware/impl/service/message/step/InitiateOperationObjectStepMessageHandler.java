/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.impl.service.message.step;

import de.adorsys.ledgers.deposit.api.domain.PaymentBO;
import de.adorsys.ledgers.middleware.api.domain.general.StepOperation;
import de.adorsys.ledgers.sca.domain.OpTypeBO;
import de.adorsys.ledgers.um.api.domain.AisConsentBO;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

import static de.adorsys.ledgers.sca.domain.OpTypeBO.*;

@Component
public class InitiateOperationObjectStepMessageHandler implements StepMessageHandler{
    @Override
    public String message(StepMessageHandlerRequest request) {
        OpTypeBO opType = request.getOpType();
        if (opType == CONSENT) {
            AisConsentBO aisConsent = (AisConsentBO) request.getOperationObject();
            return new ConsentMessageHelper(aisConsent).template();
        } else if (EnumSet.of(PAYMENT, CANCEL_PAYMENT).contains(opType)) {
            PaymentBO payment = (PaymentBO) request.getOperationObject();
            return new PaymentMessageHelper(payment.getPaymentId(), opType, payment.getPaymentType()).resolveMessage(request.isScaRequired());
        }
        AisConsentBO aisConsent = (AisConsentBO) request.getOperationObject();
        return new ConsentMessageHelper(aisConsent).exemptedTemplate();
    }

    @Override
    public StepOperation getStepOperation() {
        return StepOperation.INITIATE_OPERATION_OBJECT;
    }
}
