/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.deposit.api.domain;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertSame;


@Slf4j
class BankTransactionCodeTest {
    private static Map<String, String> productVsCode;

    static {
        productVsCode = new HashMap<>();
        productVsCode.put("instant-sepa-credit-transfers", "PMNT-IRCT-STDO");
        productVsCode.put("target-2-payments", null);
        productVsCode.put("domestic-swiss-credit-transfers-isr", null);
        productVsCode.put("pain.001-swiss-six-credit-transfers", null);
        productVsCode.put("sepa-credit-transfers", "PMNT-ICDT-STDO");
        productVsCode.put("cross-border-credit-transfers", "PMNT-ICDT-XBST");
        productVsCode.put("domestic-swiss-credit-transfers", null);
        productVsCode.put("pain.001-sepa-credit-transfers", "PMNT-ICDT-STDO");
        productVsCode.put("pain.001-cross-border-credit-transfers", "PMNT-ICDT-XBST");
    }

    @Test
    void getByPaymentProduct() {
        productVsCode
                .forEach((k, v) -> {
                    log.info("checking {}, expecting {}", k, v);
                    assertion(k);
                });
    }

    private void assertion(String product) {
        String result = BankTransactionCode.getByPaymentProduct(product);
        assertSame(result, (productVsCode.get(product)));
    }
}
