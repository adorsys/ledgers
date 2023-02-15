/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.api.domain.sca;

import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTypeTO;
import de.adorsys.ledgers.middleware.api.domain.payment.TransactionStatusTO;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SCAPaymentResponseTO extends SCAResponseTO {
    private String paymentId;
    private TransactionStatusTO transactionStatus;
    private String paymentProduct;
    private PaymentTypeTO paymentType;

    public SCAPaymentResponseTO() {
        super(SCAPaymentResponseTO.class.getSimpleName());
    }

    public SCAPaymentResponseTO(String paymentId, String transactionStatus, String paymentType, String paymentProduct) {
        this.paymentId = paymentId;
        this.transactionStatus = TransactionStatusTO.valueOf(transactionStatus);
        this.paymentType = PaymentTypeTO.valueOf(paymentType);
        this.paymentProduct = paymentProduct;
        this.setStatusDate(LocalDateTime.now());
    }
}
