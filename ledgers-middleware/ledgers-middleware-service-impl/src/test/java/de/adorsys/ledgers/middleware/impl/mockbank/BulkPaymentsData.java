package de.adorsys.ledgers.middleware.impl.mockbank;

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
