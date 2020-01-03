package de.adorsys.ledgers.app.mock;

import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTO;

public class BulkPaymentsData extends BalancesData {

    private PaymentTO bulkPayment;

    public PaymentTO getBulkPayment() {
        return bulkPayment;
    }

    public void setBulkPayment(PaymentTO bulkPayment) {
        this.bulkPayment = bulkPayment;
    }
}
