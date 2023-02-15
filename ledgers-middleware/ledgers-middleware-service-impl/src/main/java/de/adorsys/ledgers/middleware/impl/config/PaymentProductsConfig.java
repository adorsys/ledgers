/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.impl.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "ledgers.payment-products")
public class PaymentProductsConfig {
    private Set<String> instant = new HashSet<>();
    private Set<String> regular = new HashSet<>();

    public boolean isNotSupportedPaymentProduct(String paymentProduct) {
        return !instant.contains(paymentProduct) && !regular.contains(paymentProduct);
    }
}
