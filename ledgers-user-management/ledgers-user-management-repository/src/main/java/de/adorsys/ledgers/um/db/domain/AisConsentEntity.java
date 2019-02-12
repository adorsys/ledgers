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

import java.time.LocalDate;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

@Entity
@Table(name = "sca_ais_consent")
public class AisConsentEntity {

	@Id
	private String id;
	
    private String userId;

    private String tppId;

    private int frequencyPerDay;

//    @Embedded
//    @CollectionTable(name = "ais_account_access", joinColumns = @JoinColumn(name = "consent_id"))
//    private AisAccountAccessInfo access = new AisAccountAccessInfo();
	@ElementCollection
	@CollectionTable(name ="sca_ais_consent_accounts", joinColumns=@JoinColumn(name="ais_consent_entity_id"))
    private List<String> accounts;

	@ElementCollection
	@CollectionTable(name ="sca_ais_consent_balances", joinColumns=@JoinColumn(name="ais_consent_entity_id"))
	private List<String> balances;

	@ElementCollection
	@CollectionTable(name ="sca_ais_consent_transactions", joinColumns=@JoinColumn(name="ais_consent_entity_id"))
    private List<String> transactions;

    @Enumerated(EnumType.STRING)
    private AisAccountAccessType availableAccounts;

    @Enumerated(EnumType.STRING)
    private AisAccountAccessType allPsd2;
    private LocalDate validUntil;

    private boolean recurringIndicator;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getTppId() {
		return tppId;
	}

	public void setTppId(String tppId) {
		this.tppId = tppId;
	}

	public int getFrequencyPerDay() {
		return frequencyPerDay;
	}

	public void setFrequencyPerDay(int frequencyPerDay) {
		this.frequencyPerDay = frequencyPerDay;
	}

//	public AisAccountAccessInfo getAccess() {
//		return access;
//	}
//
//	public void setAccess(AisAccountAccessInfo access) {
//		this.access = access;
//	}

	public LocalDate getValidUntil() {
		return validUntil;
	}

	public void setValidUntil(LocalDate validUntil) {
		this.validUntil = validUntil;
	}

	public boolean isRecurringIndicator() {
		return recurringIndicator;
	}

	public void setRecurringIndicator(boolean recurringIndicator) {
		this.recurringIndicator = recurringIndicator;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	

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
