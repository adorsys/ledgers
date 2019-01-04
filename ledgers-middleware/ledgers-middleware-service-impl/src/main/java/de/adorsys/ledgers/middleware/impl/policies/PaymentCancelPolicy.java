package de.adorsys.ledgers.middleware.impl.policies;

import static de.adorsys.ledgers.middleware.api.domain.payment.TransactionStatusTO.ACSC;

import org.springframework.stereotype.Service;

import de.adorsys.ledgers.middleware.api.domain.payment.TransactionStatusTO;
import de.adorsys.ledgers.middleware.api.exception.PaymentProcessingMiddlewareException;

@Service
public class PaymentCancelPolicy {

	public void onCancel(String paymentId, TransactionStatusTO originalTxStatus)
			throws PaymentProcessingMiddlewareException {
		// What statuses do not allow a cancellation?
		if (originalTxStatus == ACSC) {
			throw new PaymentProcessingMiddlewareException(String.format(
					"Request for payment cancellation is forbidden as the payment with id:%s has status:%s", paymentId,
					originalTxStatus));
		}
	}

}
