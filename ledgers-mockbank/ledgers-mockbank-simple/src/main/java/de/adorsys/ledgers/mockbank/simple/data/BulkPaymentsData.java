package de.adorsys.ledgers.mockbank.simple.data;

import de.adorsys.ledgers.middleware.api.domain.payment.BulkPaymentTO;

public class BulkPaymentsData extends BalancesData {

	private BulkPaymentTO bulkPayment;
	public BulkPaymentTO getBulkPayment() {
		return bulkPayment;
	}
	public void setBulkPayment(BulkPaymentTO bulkPayment) {
		this.bulkPayment = bulkPayment;
	}
}
