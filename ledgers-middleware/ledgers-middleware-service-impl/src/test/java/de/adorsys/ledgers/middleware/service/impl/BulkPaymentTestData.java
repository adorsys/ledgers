package de.adorsys.ledgers.middleware.service.impl;

import java.util.List;

import de.adorsys.ledgers.middleware.service.domain.payment.BulkPaymentTO;

public class BulkPaymentTestData {

	private BulkPaymentTO bulkPayment;
	private List<AccountBalance> before;
	private List<AccountBalance> after;
	public BulkPaymentTO getBulkPayment() {
		return bulkPayment;
	}
	public void setBulkPayment(BulkPaymentTO bulkPayment) {
		this.bulkPayment = bulkPayment;
	}
	public List<AccountBalance> getBefore() {
		return before;
	}
	public void setBefore(List<AccountBalance> before) {
		this.before = before;
	}
	public List<AccountBalance> getAfter() {
		return after;
	}
	public void setAfter(List<AccountBalance> after) {
		this.after = after;
	}
	
	
}
