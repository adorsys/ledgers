package de.adorsys.ledgers.mockbank.simple.data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

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
