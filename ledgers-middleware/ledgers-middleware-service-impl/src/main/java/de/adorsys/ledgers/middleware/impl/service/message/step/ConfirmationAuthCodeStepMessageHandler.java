package de.adorsys.ledgers.middleware.impl.service.message.step;

import de.adorsys.ledgers.deposit.api.domain.PaymentBO;
import de.adorsys.ledgers.deposit.api.service.DepositAccountPaymentService;
import de.adorsys.ledgers.middleware.api.domain.general.StepOperation;
import de.adorsys.ledgers.sca.domain.OpTypeBO;
import de.adorsys.ledgers.sca.domain.SCAOperationBO;
import de.adorsys.ledgers.um.api.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import static de.adorsys.ledgers.sca.domain.OpTypeBO.CONSENT;

@Component
@AllArgsConstructor
public class ConfirmationAuthCodeStepMessageHandler extends BaseStepMessageHandler {

    private final DepositAccountPaymentService paymentService;
    private final UserService userService;

    @Override
    public String message(StepMessageHandlerRequest request) {
        SCAOperationBO scaOperation = request.getScaOperation();
        var template = resolveOperationTemplate(scaOperation.getOpType(), scaOperation.getOpId());
        return updateMessage(template, request);
    }

    @Override
    public StepOperation getStepOperation() {
        return StepOperation.CONFIRM_AUTH_CODE;
    }

    private String resolveOperationTemplate(OpTypeBO opType, String oprId) {
        if (opType == CONSENT) {
            var aisConsentBO = userService.loadConsent(oprId);
            return new ConsentMessageHelper(aisConsentBO).template();
        }
        PaymentBO payment = paymentService.getPaymentById(oprId);
        return new PaymentMessageHelper(oprId, opType, payment.getPaymentType()).getTanTemplate();
    }
}
