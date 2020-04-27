package de.adorsys.ledgers.middleware.impl.policies;

import de.adorsys.ledgers.deposit.api.domain.PaymentBO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentCoreDataTO;
import de.adorsys.ledgers.sca.domain.OpTypeBO;
import org.springframework.stereotype.Service;

import static de.adorsys.ledgers.sca.domain.OpTypeBO.CANCEL_PAYMENT;

@Service
public class PaymentCoreDataPolicy {

    public PaymentCoreDataTO getPaymentCoreData(OpTypeBO opType, PaymentBO payment) {
        if (opType == CANCEL_PAYMENT) {
            return getCancelPaymentCoreData(payment);
        }
        return getPaymentCoreData(payment);
    }

    private PaymentCoreDataTO getPaymentCoreData(PaymentBO payment) {
        return PaymentCoreDataPolicyHelper.getPaymentCoreDataInternal(payment);
    }

    private PaymentCoreDataTO getCancelPaymentCoreData(PaymentBO payment) {
        PaymentCoreDataTO cancel = PaymentCoreDataPolicyHelper.getPaymentCoreDataInternal(payment);
        cancel.setCancellation(true);
        return cancel;
    }
}
