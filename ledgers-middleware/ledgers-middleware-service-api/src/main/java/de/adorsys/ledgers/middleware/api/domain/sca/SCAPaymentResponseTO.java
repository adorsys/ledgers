package de.adorsys.ledgers.middleware.api.domain.sca;

import de.adorsys.ledgers.middleware.api.domain.payment.TransactionStatusTO;

public class SCAPaymentResponseTO extends SCAResponseTO {
	private String paymentId;
	private TransactionStatusTO transactionStatus;

	public String getPaymentId() {
		return paymentId;
	}

	public void setPaymentId(String paymentId) {
		this.paymentId = paymentId;
	}

	public TransactionStatusTO getTransactionStatus() {
		return transactionStatus;
	}

	public void setTransactionStatus(TransactionStatusTO transactionStatus) {
		this.transactionStatus = transactionStatus;
	}
}
