package de.adorsys.ledgers.middleware.service.impl;

import de.adorsys.ledgers.middleware.service.domain.payment.SinglePaymentTO;

public class SinglePaymentsData extends BalancesData {
	
	private SinglePaymentTO singlePayment;

	public SinglePaymentTO getSinglePayment() {
		return singlePayment;
	}
	public void setSinglePayment(SinglePaymentTO singlePayment) {
		this.singlePayment = singlePayment;
	}
}
