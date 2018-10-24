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

import java.time.LocalDate;

public class PeriodicPaymentBO extends SinglePaymentBO {
    private LocalDate startDate;
    private LocalDate endDate;
    private String executionRule;
    private FrequencyCodeBO frequency; // TODO consider using an enum similar to FrequencyCode based on the the "EventFrequency7Code" of ISO 20022
    private byte dayOfExecution; //Day here max 31
	public LocalDate getStartDate() {
		return startDate;
	}
	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}
	public LocalDate getEndDate() {
		return endDate;
	}
	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}
	public String getExecutionRule() {
		return executionRule;
	}
	public void setExecutionRule(String executionRule) {
		this.executionRule = executionRule;
	}
	public FrequencyCodeBO getFrequency() {
		return frequency;
	}
	public void setFrequency(FrequencyCodeBO frequency) {
		this.frequency = frequency;
	}
	public byte getDayOfExecution() {
		return dayOfExecution;
	}
	public void setDayOfExecution(byte dayOfExecution) {
		this.dayOfExecution = dayOfExecution;
	}
}
