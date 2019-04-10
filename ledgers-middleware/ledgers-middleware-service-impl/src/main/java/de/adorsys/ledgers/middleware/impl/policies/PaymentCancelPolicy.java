package de.adorsys.ledgers.middleware.impl.policies;

import de.adorsys.ledgers.middleware.api.domain.payment.TransactionStatusTO;
import de.adorsys.ledgers.middleware.api.exception.PaymentProcessingMiddlewareException;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Set;

import static de.adorsys.ledgers.middleware.api.domain.payment.TransactionStatusTO.*;

@Service
public class PaymentCancelPolicy {
    private final Set<TransactionStatusTO> FINAL_STATUSES = EnumSet.of(ACSC, RJCT, CANC);

    public void onCancel(String paymentId, TransactionStatusTO originalTxStatus)
            throws PaymentProcessingMiddlewareException {
        // What statuses do not allow a cancellation?
        if (FINAL_STATUSES.contains(originalTxStatus)) {
            throw new PaymentProcessingMiddlewareException(String.format(
                    "Request for payment cancellation is forbidden as the payment with id:%s has status:%s", paymentId,
                    originalTxStatus));
        }
    }

}
