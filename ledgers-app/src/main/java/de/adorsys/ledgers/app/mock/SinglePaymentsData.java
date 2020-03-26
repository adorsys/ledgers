package de.adorsys.ledgers.app.mock;

import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTO;
import lombok.Data;

@Data
public class SinglePaymentsData extends BalancesData {
    private PaymentTO singlePayment;
}
