package de.adorsys.ledgers.middleware.impl.converter;

import de.adorsys.ledgers.deposit.api.domain.PaymentBO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTypeTO;
import de.adorsys.ledgers.middleware.api.domain.sca.SCAPaymentResponseTO;

public interface PainPaymentConverter {
    PaymentBO toPaymentBO(String payment, PaymentTypeTO paymentType);

    String toPayload(SCAPaymentResponseTO response);
}
