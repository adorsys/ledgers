package de.adorsys.ledgers.app.mock;

import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTO;

public class SinglePaymentsData extends BalancesData {

    private PaymentTO singlePayment;

    public PaymentTO getSinglePayment() {
        return singlePayment;
    }

    public void setSinglePayment(PaymentTO singlePayment) {
        this.singlePayment = singlePayment;
    }
}
