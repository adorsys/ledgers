package de.adorsys.ledgers.deposit.domain;

import java.time.LocalDate;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.jetbrains.annotations.NotNull;

@Entity
public class PaymentEntity {
	/*
	 * The is id of the payment request
	 */
	@Id
	private String paymentId;

	/*
	 * If this element equals "true", the PSU prefers only one booking entry. If
	 * this element equals "false", the PSU prefers individual booking of all
	 * contained individual transactions. The ASPSP will follow this preference
	 * according to contracts agreed on with the PSU.
	 * 
	 * This is only used for payments of type de.adorsys.ledgers.deposit.domain.PaymentTypeBO.BULK
	 */
	private Boolean batchBookingPreferred;

	private LocalDate requestedExecutionDate;

	private PaymentTypeBO paymentType;

    private LocalDate startDate;
    private LocalDate endDate;
    private String executionRule;
    private FrequencyCode frequency; // TODO consider using an enum similar to FrequencyCode based on the the "EventFrequency7Code" of ISO 20022
    private byte dayOfExecution; //Day here max 31
	
    @NotNull
    private AccountReference debtorAccount;

    private TransactionStatus transactionStatus;

	@OneToMany
	private List<PaymentTarget> targets;

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

	public FrequencyCode getFrequency() {
		return frequency;
	}

	public void setFrequency(FrequencyCode frequency) {
		this.frequency = frequency;
	}

	public byte getDayOfExecution() {
		return dayOfExecution;
	}

	public void setDayOfExecution(byte dayOfExecution) {
		this.dayOfExecution = dayOfExecution;
	}

	public AccountReference getDebtorAccount() {
		return debtorAccount;
	}

	public void setDebtorAccount(AccountReference debtorAccount) {
		this.debtorAccount = debtorAccount;
	}

	public TransactionStatus getTransactionStatus() {
		return transactionStatus;
	}

	public void setTransactionStatus(TransactionStatus transactionStatus) {
		this.transactionStatus = transactionStatus;
	}

	public List<PaymentTarget> getTargets() {
		return targets;
	}

	public void setTargets(List<PaymentTarget> targets) {
		this.targets = targets;
	}
}
