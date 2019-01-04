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

package de.adorsys.ledgers.middleware.api.domain.um;

import java.time.LocalDate;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Ais consent request", value = "AisConsentRequest")
public class AisConsentTO {

    @ApiModelProperty(value = "The consent id", required = true)
    private String id;

    @ApiModelProperty(value = "Corresponding PSU", required = true)
    private String userId;

    @ApiModelProperty(value = "ID of the corresponding TPP.", required = true, example = "testTPP")
    private String tppId;

    @ApiModelProperty(value = "Maximum frequency for an access per day. For a once-off access, this attribute is set to 1", required = true, example = "4")
    private int frequencyPerDay;

    @ApiModelProperty(value = "Set of accesses given by psu for this account", required = true)
    private AisAccountAccessInfoTO access;

    @ApiModelProperty(value = "Consent`s expiration date. The content is the local ASPSP date in ISODate Format", required = true, example = "2020-10-10")
    private LocalDate validUntil;

    @ApiModelProperty(value = "'true', if the consent is for recurring access to the account data , 'false', if the consent is for one access to the account data", required = true, example = "false")
    private boolean recurringIndicator;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

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

	public AisAccountAccessInfoTO getAccess() {
		return access;
	}

	public void setAccess(AisAccountAccessInfoTO access) {
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
    
    
}
