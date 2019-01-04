package de.adorsys.ledgers.deposit.api.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class PaymentBO {
    private String paymentId;
    private Boolean batchBookingPreferred;
    private LocalDate requestedExecutionDate;
    private LocalTime requestedExecutionTime;
    private PaymentTypeBO paymentType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String executionRule;
    private FrequencyCodeBO frequency;
    private Integer dayOfExecution; //Day here max 31
    private AccountReferenceBO debtorAccount;
    private TransactionStatusBO transactionStatus;
    private List<PaymentTargetBO> targets = new ArrayList<>();

    public PaymentBO() {
    }

    public PaymentBO(String paymentId, Boolean batchBookingPreferred, LocalDate requestedExecutionDate, LocalTime requestedExecutionTime, PaymentTypeBO paymentType, LocalDate startDate, LocalDate endDate, String executionRule, FrequencyCodeBO frequency, Integer dayOfExecution, AccountReferenceBO debtorAccount, TransactionStatusBO transactionStatus, List<PaymentTargetBO> targets) {
        this.paymentId = paymentId;
        this.batchBookingPreferred = batchBookingPreferred;
        this.requestedExecutionDate = requestedExecutionDate;
        this.requestedExecutionTime = requestedExecutionTime;
        this.paymentType = paymentType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.executionRule = executionRule;
        this.frequency = frequency;
        this.dayOfExecution = dayOfExecution;
        this.debtorAccount = debtorAccount;
        this.transactionStatus = transactionStatus;
        this.targets = targets;
    }

    //Getters-Setters

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

    public Integer getDayOfExecution() {
        return dayOfExecution;
    }

    public void setDayOfExecution(Integer dayOfExecution) {
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

    public List<PaymentTargetBO> getTargets() {
        return targets;
    }

    public void setTargets(List<PaymentTargetBO> targets) {
        this.targets = targets;
    }
}
