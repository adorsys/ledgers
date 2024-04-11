/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.impl.service.validation;

import de.adorsys.ledgers.deposit.api.domain.PaymentBO;
import de.adorsys.ledgers.deposit.api.service.DepositAccountPaymentService;
import de.adorsys.ledgers.middleware.api.exception.MiddlewareModuleException;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.util.Ids;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class PaymentIdValidator extends AbstractPaymentValidator {
    private final DepositAccountPaymentService paymentService;

    @Override
    public void check(PaymentBO payment, UserBO user) {
        validateAndUpdate(payment.getPaymentId(), payment::setPaymentId, true);
        payment.getTargets().forEach(t -> validateAndUpdate(t.getPaymentId(), t::setPaymentId, false));
        checkNext(payment, user);
    }

    @SuppressWarnings("PMD.AvoidReassigningParameters")
    private void validateAndUpdate(String id, Consumer<String> idConsumer, boolean isDebtorPart) {
        if (StringUtils.isBlank(id)) {
            id = generateId(idConsumer);
        }

        while (validateId(id, isDebtorPart)) {
            id = generateId(idConsumer);
        }
    }

    private boolean validateId(String id, boolean isDebtorPart) {
        boolean exists = isDebtorPart
                                 ? paymentService.existingPaymentById(id)
                                 : paymentService.existingTargetById(id);

        if (isDebtorPart && exists) {
            throw MiddlewareModuleException.paymentValidationException("Payment with id: %s already exists!");
        }
        return exists;
    }

    private String generateId(Consumer<String> idConsumer) {
        String generatedId = Ids.id();
        idConsumer.accept(generatedId);
        return generatedId;
    }
}
