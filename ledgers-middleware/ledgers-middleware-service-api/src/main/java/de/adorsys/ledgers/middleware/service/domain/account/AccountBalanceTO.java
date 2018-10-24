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

package de.adorsys.ledgers.middleware.service.domain.account;

import de.adorsys.ledgers.middleware.service.domain.payment.AmountTO;

import java.time.LocalDate;
import java.time.LocalDateTime;


public class AccountBalanceTO {
    private AmountTO amount;
    private BalanceTypeTO balanceType;
    private LocalDateTime lastChangeDateTime;
    private LocalDate referenceDate;
    private String lastCommittedTransaction;

    public AmountTO getAmount() {
        return amount;
    }

    public void setAmount(AmountTO amount) {
        this.amount = amount;
    }

    public BalanceTypeTO getBalanceType() {
        return balanceType;
    }

    public void setBalanceType(BalanceTypeTO balanceType) {
        this.balanceType = balanceType;
    }

    public LocalDateTime getLastChangeDateTime() {
        return lastChangeDateTime;
    }

    public void setLastChangeDateTime(LocalDateTime lastChangeDateTime) {
        this.lastChangeDateTime = lastChangeDateTime;
    }

    public LocalDate getReferenceDate() {
        return referenceDate;
    }

    public void setReferenceDate(LocalDate referenceDate) {
        this.referenceDate = referenceDate;
    }

    public String getLastCommittedTransaction() {
        return lastCommittedTransaction;
    }

    public void setLastCommittedTransaction(String lastCommittedTransaction) {
        this.lastCommittedTransaction = lastCommittedTransaction;
    }
}
