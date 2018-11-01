package de.adorsys.ledgers.deposit.db.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters.LocalDateConverter;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters.LocalTimeConverter;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;

@Entity
public class Payment {
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

    @JsonDeserialize(using = LocalDateDeserializer.class)
	@Convert(converter=LocalDateConverter.class)
	private LocalDate requestedExecutionDate;

	@Convert(converter=LocalTimeConverter.class)
    private LocalTime requestedExecutionTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
	private PaymentType paymentType;

    @JsonDeserialize(using = LocalDateDeserializer.class)
	@Convert(converter=LocalDateConverter.class)
    private LocalDate startDate;

    @JsonDeserialize(using = LocalDateDeserializer.class)
	@Convert(converter=LocalDateConverter.class)
    private LocalDate endDate;

    private String executionRule;

    @Enumerated(EnumType.STRING)
    private FrequencyCode frequency; // TODO consider using an enum similar to FrequencyCode based on the the "EventFrequency7Code" of ISO 20022

    private int dayOfExecution; //Day here max 31
	
    @NotNull
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name="iban",column=@Column(name="DEBT_IBAN")),
        @AttributeOverride(name="bban",column=@Column(name="DEBT_BBAN")),
        @AttributeOverride(name="pan",column=@Column(name="DEBT_PAN")),
        @AttributeOverride(name="maskedPan",column=@Column(name="DEBT_MASKED_PAN")),
        @AttributeOverride(name="msisdn",column=@Column(name="DEBT_MSISDN"))
    })
    @Column(nullable = false)
    private AccountReference debtorAccount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus transactionStatus;

	@OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.ALL})
	private List<PaymentTarget> targets = new ArrayList<>();

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

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
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

    public int getDayOfExecution() {
        return dayOfExecution;
    }

    public void setDayOfExecution(int dayOfExecution) {
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
