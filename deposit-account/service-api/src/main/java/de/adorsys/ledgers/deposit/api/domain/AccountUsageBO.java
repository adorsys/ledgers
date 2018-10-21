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

package de.adorsys.ledgers.deposit.api.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum AccountUsageBO {
    PRIV("PRIV"),
    ORGA("ORGA");

    private static final Map<String, AccountUsageBO> container = new HashMap<>();
    private String value;

    
    private AccountUsageBO(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    
    public static Optional<AccountUsageBO> getByValue(String value) {
        return Optional.ofNullable(container.get(value));
    }

    static {
        AccountUsageBO[] var0 = values();

        for (AccountUsageBO usageType : var0) {
            container.put(usageType.getValue(), usageType);
        }
    }
}
