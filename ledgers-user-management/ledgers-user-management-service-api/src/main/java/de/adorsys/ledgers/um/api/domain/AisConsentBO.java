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

package de.adorsys.ledgers.um.api.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AisConsentBO {
    private String id;
    private String userId;
    private String tppId;
    private int frequencyPerDay;
    private AisAccountAccessInfoBO access;
    private LocalDate validUntil;
    private boolean recurringIndicator;

    public AisConsentBO(String ibanForAccess, int frequencyPerDay, boolean recurringIndicator, String userId) {
        this.access = new AisAccountAccessInfoBO();
        List<String> list = Collections.singletonList(ibanForAccess);
        this.access.setAccounts(list);
        this.access.setTransactions(list);
        this.access.setBalances(list);
        this.frequencyPerDay = frequencyPerDay;
        this.recurringIndicator = recurringIndicator;
        this.userId = userId;
    }
}
