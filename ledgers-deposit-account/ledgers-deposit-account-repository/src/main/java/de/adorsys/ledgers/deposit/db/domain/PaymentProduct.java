/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.ledgers.deposit.db.domain;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
/**
 * @deprecated Shall be removed in v2.5
 */
@Deprecated
public enum PaymentProduct {
    SEPA("sepa-credit-transfers"),
    INSTANT_SEPA("instant-sepa-credit-transfers"),
    TARGET2("target-2-payments"),
    CROSS_BORDER("cross-border-credit-transfers");

    private String value;

    private static Map<String, PaymentProduct> container = new HashMap<>();

    static {
        Arrays.stream(values())
            .forEach(product -> container.put(product.getValue(), product));
    }

    PaymentProduct(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static PaymentProduct getByValue(String value) {
        return container.get(value);
    }
}
