package de.adorsys.ledgers.deposit.api.domain;

import java.time.LocalDate;
import java.time.LocalTime;

public class PaymentOrderDetailsBO {
	private String paymentId;
    private Boolean batchBookingPreferred;
    private LocalDate requestedExecutionDate;
    private LocalTime requestedExecutionTime;
    private PaymentTypeBO paymentType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String executionRule;
    private FrequencyCodeBO frequency;
    private int dayOfExecution; //Day here max 31
    private AccountReferenceBO debtorAccount;
    private TransactionStatusBO transactionStatus;
    
    
	public PaymentOrderDetailsBO() {
	}

	public PaymentOrderDetailsBO(PaymentBO p) {
		this.paymentId = p.getPaymentId();
		this.batchBookingPreferred = p.getBatchBookingPreferred();
		this.requestedExecutionDate = p.getRequestedExecutionDate();
		this.requestedExecutionTime = p.getRequestedExecutionTime();
		this.paymentType = p.getPaymentType();
		this.startDate = p.getStartDate();
		this.endDate = p.getEndDate();
		this.executionRule = p.getExecutionRule();
		this.frequency = p.getFrequency();
		this.dayOfExecution = p.getDayOfExecution();
		this.debtorAccount = p.getDebtorAccount();
		this.transactionStatus = p.getTransactionStatus();
	}
	
	public String getPaymentId() {
		return paymentId;
	}
	public void setPaymentId(String paymentId) {
		this.paymentId = paymentId;
	}
	public Boolean getBatchBookingPreferred() {
		return batchBookingPreferred;
	}
	public void setBatchBookingPreferred(Boolean batchBookingPreferred) {
		this.batchBookingPreferred = batchBookingPreferred;
	}
	public LocalDate getRequestedExecutionDate() {
		return requestedExecutionDate;
	}
	public void setRequestedExecutionDate(LocalDate requestedExecutionDate) {
		this.requestedExecutionDate = requestedExecutionDate;
	}
	public LocalTime getRequestedExecutionTime() {
		return requestedExecutionTime;
	}
	public void setRequestedExecutionTime(LocalTime requestedExecutionTime) {
		this.requestedExecutionTime = requestedExecutionTime;
	}
	public PaymentTypeBO getPaymentType() {
		return paymentType;
	}
	public void setPaymentType(PaymentTypeBO paymentType) {
		this.paymentType = paymentType;
	}
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
	public int getDayOfExecution() {
		return dayOfExecution;
	}
	public void setDayOfExecution(int dayOfExecution) {
		this.dayOfExecution = dayOfExecution;
	}
	public AccountReferenceBO getDebtorAccount() {
		return debtorAccount;
	}
	public void setDebtorAccount(AccountReferenceBO debtorAccount) {
		this.debtorAccount = debtorAccount;
	}
	public TransactionStatusBO getTransactionStatus() {
		return transactionStatus;
	}
	public void setTransactionStatus(TransactionStatusBO transactionStatus) {
		this.transactionStatus = transactionStatus;
	}

}
