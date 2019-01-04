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

package de.adorsys.ledgers.um.db.domain;

import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Embeddable
public class AisAccountAccessInfo {

	@ElementCollection
	@CollectionTable(name ="sca_ais_consent_accounts")
    private List<String> accounts;

	@ElementCollection
	@CollectionTable(name ="sca_ais_consent_balances")
	private List<String> balances;

	@ElementCollection
	@CollectionTable(name ="sca_ais_consent_transactions")
    private List<String> transactions;

    @Enumerated(EnumType.STRING)
    private AisAccountAccessType availableAccounts;

    @Enumerated(EnumType.STRING)
    private AisAccountAccessType allPsd2;

	public List<String> getAccounts() {
		return accounts;
	}

	public void setAccounts(List<String> accounts) {
		this.accounts = accounts;
	}

	public List<String> getBalances() {
		return balances;
	}

	public void setBalances(List<String> balances) {
		this.balances = balances;
	}

	public List<String> getTransactions() {
		return transactions;
	}

	public void setTransactions(List<String> transactions) {
		this.transactions = transactions;
	}

	public AisAccountAccessType getAvailableAccounts() {
		return availableAccounts;
	}

	public void setAvailableAccounts(AisAccountAccessType availableAccounts) {
		this.availableAccounts = availableAccounts;
	}

	public AisAccountAccessType getAllPsd2() {
		return allPsd2;
	}

	public void setAllPsd2(AisAccountAccessType allPsd2) {
		this.allPsd2 = allPsd2;
	}
    
    
}
