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

import java.time.LocalDate;

public class AisConsentBO {
	
	private String id;

    private String userId;

    private String tppId;

    private int frequencyPerDay;

    private AisAccountAccessInfoBO access;

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

	public AisAccountAccessInfoBO getAccess() {
		return access;
	}

	public void setAccess(AisAccountAccessInfoBO access) {
		this.access = access;
	}

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
}
