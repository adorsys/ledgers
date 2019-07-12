package de.adorsys.ledgers.middleware.impl.policies;

import de.adorsys.ledgers.middleware.api.domain.payment.TransactionStatusTO;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareModuleException;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.Set;

import static de.adorsys.ledgers.middleware.api.domain.payment.TransactionStatusTO.*;
import static de.adorsys.ledgers.middleware.api.exception.MiddlewareErrorCode.PAYMENT_PROCESSING_FAILURE;

@Service
public class PaymentCancelPolicy {
    private final Set<TransactionStatusTO> FINAL_STATUSES = EnumSet.of(ACSC, RJCT, CANC);

    public void onCancel(String paymentId, TransactionStatusTO originalTxStatus) {
        // What statuses do not allow a cancellation?
        if (FINAL_STATUSES.contains(originalTxStatus)) {
            throw MiddlewareModuleException.builder()
                          .errorCode(PAYMENT_PROCESSING_FAILURE)
                          .devMsg(String.format("Request for payment cancellation is forbidden as the payment with id:%s has status:%s", paymentId, originalTxStatus))
                          .build();
        }
    }
}
