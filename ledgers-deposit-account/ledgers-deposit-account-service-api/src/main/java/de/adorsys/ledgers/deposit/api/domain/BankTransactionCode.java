/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.deposit.api.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public enum BankTransactionCode {
    SEPA("PMNT-ICDT-STDO"), //for credit transfers,
    INSTANT_SEPA("PMNT-IRCT-STDO"),// for instant credit transfers
    CROSS_BORDER("PMNT-ICDT-XBST"), //for cross-border credit transfers
    INSTANT_CROSS_BORDER("PMNT-IRCT-XBST"), // for cross-border real time credit transfers and
    SPECIFIC("PMNT-MCOP-OTHR"); //for specific standing orders which have a dynamical amount to move left funds e.g. on month end to a saving account.

    private static final Map<String, BankTransactionCode> container = new HashMap<>();
    private String value;

    BankTransactionCode(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public static Optional<BankTransactionCode> getByValue(String value) {
        return Optional.ofNullable(container.get(value));
    }

    public static String getByPaymentProduct(String paymentProduct) {
        return resolveByProduct(paymentProduct, "sepa", SEPA::getValue, INSTANT_SEPA::getValue)
                       .orElseGet(() -> resolveByProduct(paymentProduct, "cross-border", CROSS_BORDER::getValue, INSTANT_CROSS_BORDER::getValue)
                                                .orElse(null));
    }

    private static Optional<String> resolveByProduct(String paymentProduct, String attribute, Supplier<String> supplier, Supplier<String> instantSupplier) {
        return Optional.ofNullable(paymentProduct)
                       .map(p -> {
                           if (paymentProduct.contains(attribute)) {
                               return checkInstant(p, instantSupplier)
                                              .orElse(supplier.get());
                           }
                           return null;
                       });
    }

    private static Optional<String> checkInstant(String paymentProduct, Supplier<String> instantSupplier) {
        return paymentProduct.contains("instant")
                       ? Optional.of(instantSupplier.get())
                       : Optional.empty();
    }

    static {
        BankTransactionCode[] var0 = values();

        for (BankTransactionCode code : var0) {
            container.put(code.getValue(), code);
        }
    }
}
