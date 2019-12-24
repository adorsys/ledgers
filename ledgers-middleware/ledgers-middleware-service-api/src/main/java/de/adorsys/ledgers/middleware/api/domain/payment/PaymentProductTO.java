package de.adorsys.ledgers.middleware.api.domain.payment;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @deprecated Shall be removed in v2.5
 */
@Deprecated
public enum PaymentProductTO {
    SEPA("sepa-credit-transfers"),
    INSTANT_SEPA("instant-sepa-credit-transfers"),
    TARGET2("target-2-payments"),
    CROSS_BORDER("cross-border-credit-transfers"),
    DOMESTIC_SWISS("domestic-swiss-credit-transfers"),
    DOMESTIC_SWISS_ISR("domestic-swiss-credit-transfers-isr"),
    PAIN_SEPA("pain.001-sepa-credit-transfers"),
    PAIN_CROSS_BORDER("pain.001-cross-border-credit-transfers"),
    PAIN_SWISS_SIX("pain.001-swiss-six-credit-transfers");

    private String value;

    private static Map<String, PaymentProductTO> container = new HashMap<>();

    static {
        for (PaymentProductTO product : values()) {
            container.put(product.getValue(), product);
        }
    }

    PaymentProductTO(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Optional<PaymentProductTO> getByValue(String value) {
        return Optional.ofNullable(container.get(value));
    }
}
