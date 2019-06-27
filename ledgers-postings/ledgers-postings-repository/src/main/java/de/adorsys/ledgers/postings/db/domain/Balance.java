package de.adorsys.ledgers.postings.db.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Balance {
	private BalanceSide balanceSide;
	private Amount amount;
    private BalanceType balanceType;
    private LocalDateTime lastChangeDateTime;
    private LocalDate referenceDate;
    private String lastCommittedTransaction;
	
	public Balance() {
	}
	
	public Balance(BalanceSide balanceSide, Amount amount) {
		this.balanceSide = balanceSide;
		this.amount = amount;
	}

	public BalanceSide getBalanceSide() {
		return balanceSide;
	}
	public void setBalanceSide(BalanceSide balanceSide) {
		this.balanceSide = balanceSide;
	}

	public Amount getAmount() {
		return amount;
	}

	public void setAmount(Amount amount) {
		this.amount = amount;
	}

	public BalanceType getBalanceType() {
		return balanceType;
	}

	public void setBalanceType(BalanceType balanceType) {
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
