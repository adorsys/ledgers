package de.adorsys.ledgers.middleware.impl.policies;

import org.springframework.stereotype.Service;

import de.adorsys.ledgers.deposit.api.domain.PaymentBO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentCoreDataTO;

@Service
public class PaymentCoreDataPolicy {

	public PaymentCoreDataTO getPaymentCoreData(PaymentBO payment) {
		return PaymentCoreDataPolicyHelper.getPaymentCoreDataInternal(payment);
	}

	public PaymentCoreDataTO getCancelPaymentCoreData(PaymentBO payment) {
		PaymentCoreDataTO cancel = PaymentCoreDataPolicyHelper.getPaymentCoreDataInternal(payment);
		cancel.setCancellation(true);
		return cancel;
	}
}
