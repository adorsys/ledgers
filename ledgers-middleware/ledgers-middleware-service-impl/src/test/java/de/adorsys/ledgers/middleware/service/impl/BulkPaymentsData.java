package de.adorsys.ledgers.middleware.service.impl;

import de.adorsys.ledgers.middleware.service.domain.payment.BulkPaymentTO;

public class BulkPaymentsData extends BalancesData {

	private BulkPaymentTO bulkPayment;
	public BulkPaymentTO getBulkPayment() {
		return bulkPayment;
	}
	public void setBulkPayment(BulkPaymentTO bulkPayment) {
		this.bulkPayment = bulkPayment;
	}
}
