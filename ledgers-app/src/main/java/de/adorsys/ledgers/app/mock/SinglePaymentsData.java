package de.adorsys.ledgers.app.mock;

import de.adorsys.ledgers.middleware.api.domain.payment.SinglePaymentTO;

public class SinglePaymentsData extends BalancesData {

    private SinglePaymentTO singlePayment;

    public SinglePaymentTO getSinglePayment() {
        return singlePayment;
    }

    public void setSinglePayment(SinglePaymentTO singlePayment) {
        this.singlePayment = singlePayment;
    }
}
