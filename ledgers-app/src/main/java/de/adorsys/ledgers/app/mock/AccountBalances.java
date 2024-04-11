/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.app.mock;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AccountBalances {
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
	private LocalDateTime refTime;
	private List<AccountBalance> balances = new ArrayList<>();

	public LocalDateTime getRefTime() {
		return refTime;
	}
	public void setRefTime(LocalDateTime refTime) {
		this.refTime = refTime;
	}
	public List<AccountBalance> getBalances() {
		return balances;
	}
	public void setBalances(List<AccountBalance> balances) {
		this.balances = balances;
	}
}
