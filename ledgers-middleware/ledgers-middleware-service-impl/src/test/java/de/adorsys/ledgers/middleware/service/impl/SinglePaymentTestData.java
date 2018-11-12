package de.adorsys.ledgers.middleware.service.impl;

import java.math.BigDecimal;

import de.adorsys.ledgers.middleware.service.domain.payment.SinglePaymentTO;

public class SinglePaymentTestData {
	
	private SinglePaymentTO singlePayment;
	
	// Balance before tests
	private BigDecimal balanceDebitAccountBefore;
	private BigDecimal balanceCreditAccountBefore;

	// Balance after tests
	private BigDecimal balanceDebitAccountAfter;
	private BigDecimal balanceCreditAccountAfter;
	public SinglePaymentTO getSinglePayment() {
		return singlePayment;
	}
	public void setSinglePayment(SinglePaymentTO singlePayment) {
		this.singlePayment = singlePayment;
	}
	public BigDecimal getBalanceDebitAccountBefore() {
		return balanceDebitAccountBefore;
	}
	public void setBalanceDebitAccountBefore(BigDecimal balanceDebitAccountBefore) {
		this.balanceDebitAccountBefore = balanceDebitAccountBefore;
	}
	public BigDecimal getBalanceCreditAccountBefore() {
		return balanceCreditAccountBefore;
	}
	public void setBalanceCreditAccountBefore(BigDecimal balanceCreditAccountBefore) {
		this.balanceCreditAccountBefore = balanceCreditAccountBefore;
	}
	public BigDecimal getBalanceDebitAccountAfter() {
		return balanceDebitAccountAfter;
	}
	public void setBalanceDebitAccountAfter(BigDecimal balanceDebitAccountAfter) {
		this.balanceDebitAccountAfter = balanceDebitAccountAfter;
	}
	public BigDecimal getBalanceCreditAccountAfter() {
		return balanceCreditAccountAfter;
	}
	public void setBalanceCreditAccountAfter(BigDecimal balanceCreditAccountAfter) {
		this.balanceCreditAccountAfter = balanceCreditAccountAfter;
	}
}
