package de.adorsys.ledgers.middleware.api.domain.sca;

import de.adorsys.ledgers.middleware.api.domain.payment.PaymentProductTO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTypeTO;
import de.adorsys.ledgers.middleware.api.domain.payment.TransactionStatusTO;

public class SCAPaymentResponseTO extends SCAResponseTO {
	private String paymentId;
	private TransactionStatusTO transactionStatus;
	private PaymentProductTO paymentProduct;
	private PaymentTypeTO paymentType;

	public SCAPaymentResponseTO() {
		super(SCAPaymentResponseTO.class.getSimpleName());
	}

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

	public PaymentProductTO getPaymentProduct() {
		return paymentProduct;
	}

	public void setPaymentProduct(PaymentProductTO paymentProduct) {
		this.paymentProduct = paymentProduct;
	}

	public PaymentTypeTO getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(PaymentTypeTO paymentType) {
		this.paymentType = paymentType;
	}
	
}
