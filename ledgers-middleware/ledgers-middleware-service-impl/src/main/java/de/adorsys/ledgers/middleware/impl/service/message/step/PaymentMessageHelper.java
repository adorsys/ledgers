package de.adorsys.ledgers.middleware.impl.service.message.step;

import de.adorsys.ledgers.deposit.api.domain.PaymentTypeBO;
import de.adorsys.ledgers.sca.domain.OpTypeBO;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PaymentMessageHelper {

    private static final String TAN_MESSAGE_TEMPLATE = "The TAN for your %s %s order # %s is: ";
    private static final String EXEMPTED_MESSAGE_TEMPLATE = "Your %s %s order # %s is scheduled";

    private final String paymentId;
    private final OpTypeBO opType;
    private final PaymentTypeBO paymentType;

    public String getTanTemplate() {
        return getTemplate(false);
    }

    public String getExemptedTemplate() {
        return getTemplate(true);
    }

    public String resolveMessage(boolean isScaRequired) {
        return isScaRequired ? this.getTanTemplate() :
                       this.getExemptedTemplate();
    }

    private String getTemplate(boolean isExempted) {
        String paymentTyp = OpTypeBO.CANCEL_PAYMENT == opType ? "" : paymentType.name();
        String operationType = OpTypeBO.CANCEL_PAYMENT == opType ? "Payment Cancellation" : "Payment";
        return isExempted
                       ? String.format(EXEMPTED_MESSAGE_TEMPLATE, paymentTyp, operationType, paymentId)
                       : String.format(TAN_MESSAGE_TEMPLATE, paymentTyp, operationType, paymentId) + "%s";
    }
}
