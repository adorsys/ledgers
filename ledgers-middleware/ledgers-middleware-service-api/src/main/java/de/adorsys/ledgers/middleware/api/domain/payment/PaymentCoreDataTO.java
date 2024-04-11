/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

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
}
