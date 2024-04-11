/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.deposit.api.domain;

public enum PaymentTypeBO {
    SINGLE("payments"),
    BULK("bulk-payments"),
    PERIODIC("periodic-payments");

    private String paymentType;

    
    PaymentTypeBO(String paymentType) {
        this.paymentType = paymentType;
    }

    public String getPaymentType() {
        return paymentType;
    }
}
