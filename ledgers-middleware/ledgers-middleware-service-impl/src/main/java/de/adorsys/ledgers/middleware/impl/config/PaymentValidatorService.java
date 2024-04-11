/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.impl.config;

import de.adorsys.ledgers.deposit.api.domain.PaymentBO;
import de.adorsys.ledgers.middleware.impl.service.validation.AbstractPaymentValidator;
import de.adorsys.ledgers.um.api.domain.UserBO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentValidatorService {
    private final List<AbstractPaymentValidator> validators;

    public void validate(PaymentBO payment, UserBO user) {
        validators.forEach(v -> v.check(payment, user));
    }
}
