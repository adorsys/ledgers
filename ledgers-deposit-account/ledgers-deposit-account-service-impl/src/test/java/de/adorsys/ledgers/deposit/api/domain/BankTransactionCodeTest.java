package de.adorsys.ledgers.deposit.api.domain;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class BankTransactionCodeTest {
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
    public void getByPaymentProduct() {
        productVsCode
                .forEach((k, v) -> {
                    log.info("checking {}, expecting {}", k, v);
                    assertion(k);
                });
    }

    private void assertion(String product) {
        String result = BankTransactionCode.getByPaymentProduct(product);
        assertThat(result == (productVsCode.get(product))).isTrue();
    }
}
