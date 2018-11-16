package de.adorsys.ledgers.middleware.service.impl;

import java.math.BigDecimal;

public class AccountBalance {
	private BigDecimal balance;
	private String accNbr;
	public BigDecimal getBalance() {
		return balance;
	}
	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}
	public String getAccNbr() {
		return accNbr;
	}
	public void setAccNbr(String accNbr) {
		this.accNbr = accNbr;
	}

}
