package de.adorsys.ledgers.middleware.api.domain.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCoreDataTO {
    private static final String TAN_MESSAGE_TEMPLATE = "The TAN for your %s %s order # %s is: ";
    private static final String EXEMPTED_MESSAGE_TEMPLATE = "Your %s %s order # %s is scheduled";

    private String paymentId;
    private String creditorName;
    private String creditorIban;
    private String amount;
    private String currency;

    // Periodic
    private String dayOfExecution;
    private String executionRule;
    private String frequency;

    private String paymentType;

    // Bulk
    private String paymentsSize;

    // Bulk, Future Dated
    private String requestedExecutionDate;

    private boolean cancellation;

    private String paymentProduct;

    public String getTanTemplate() {
        return getTemplate(false);
    }

    public String getExemptedTemplate() {
        return getTemplate(true);
    }

    private String getTemplate(boolean isExempted) {
        String paymentTyp = cancellation
                                    ? ""
                                    : paymentType;
        String operationType = cancellation
                                       ? "Payment Cancellation"
                                       : "Payment";
        return isExempted
                       ? String.format(EXEMPTED_MESSAGE_TEMPLATE, paymentTyp, operationType, paymentId)
                       : String.format(TAN_MESSAGE_TEMPLATE, paymentTyp, operationType, paymentId) + "%s";
    }

    public String resolveMessage(boolean isScaRequired) {
        return isScaRequired
                       ? this.getTanTemplate()
                       : this.getExemptedTemplate();
    }
}
