package de.adorsys.ledgers.middleware.impl.service.validation;

import de.adorsys.ledgers.deposit.api.domain.PaymentBO;
import de.adorsys.ledgers.um.api.domain.UserBO;

public abstract class AbstractPaymentValidator {
    private AbstractPaymentValidator next;

    public AbstractPaymentValidator next(AbstractPaymentValidator next) {
        this.next = next;
        return next;
    }

    public abstract void check(PaymentBO payment, UserBO user);

    protected void checkNext(PaymentBO payment, UserBO user) {
        if (next != null) {
            next.check(payment, user);
        }
    }
}
