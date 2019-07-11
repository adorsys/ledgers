package de.adorsys.ledgers.middleware.api.domain.sca;

import de.adorsys.ledgers.middleware.api.domain.payment.PaymentProductTO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTypeTO;
import de.adorsys.ledgers.middleware.api.domain.payment.TransactionStatusTO;
import lombok.Data;

@Data
public class SCAPaymentResponseTO extends SCAResponseTO {
	private String paymentId;
	private TransactionStatusTO transactionStatus;
	private PaymentProductTO paymentProduct;
	private PaymentTypeTO paymentType;

	public SCAPaymentResponseTO() {
		super(SCAPaymentResponseTO.class.getSimpleName());
	}
}
