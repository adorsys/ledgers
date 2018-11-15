package de.adorsys.ledgers.middleware.service.impl;

import java.math.BigDecimal;

public class AccountBalance {
	private BigDecimal balance;
	private String iban;
	public BigDecimal getBalance() {
		return balance;
	}
	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}
	public String getIban() {
		return iban;
	}
	public void setIban(String iban) {
		this.iban = iban;
	}
}
