package de.adorsys.ledgers.middleware.service.domain.payment;

import de.adorsys.ledgers.middleware.service.domain.account.AccountReferenceTO;
import de.adorsys.ledgers.middleware.service.domain.general.AddressTO;

import java.time.LocalDate;
import java.time.LocalTime;

public class PeriodicPaymentTO extends SinglePaymentTO {
    private LocalDate startDate;
    private LocalDate endDate;
    private String executionRule;
    private FrequencyCodeTO frequency;
    private int dayOfExecution;

    public PeriodicPaymentTO() {
    }

    public PeriodicPaymentTO(String paymentId, String endToEndIdentification, AccountReferenceTO debtorAccount, AmountTO instructedAmount, AccountReferenceTO creditorAccount, String creditorAgent, String creditorName, AddressTO creditorAddress, String remittanceInformationUnstructured, TransactionStatusTO paymentStatus, PaymentProductTO paymentProduct, LocalDate requestedExecutionDate, LocalTime requestedExecutionTime, LocalDate startDate, LocalDate endDate, String executionRule, FrequencyCodeTO frequency, int dayOfExecution) {
        super(paymentId, endToEndIdentification, debtorAccount, instructedAmount, creditorAccount, creditorAgent, creditorName, creditorAddress, remittanceInformationUnstructured, paymentStatus, paymentProduct, requestedExecutionDate, requestedExecutionTime);
        this.startDate = startDate;
        this.endDate = endDate;
        this.executionRule = executionRule;
        this.frequency = frequency;
        this.dayOfExecution = dayOfExecution;
    }

    //Getters-Setters
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

    public FrequencyCodeTO getFrequency() {
        return frequency;
    }

    public void setFrequency(FrequencyCodeTO frequency) {
        this.frequency = frequency;
    }

    public int getDayOfExecution() {
        return dayOfExecution;
    }

    public void setDayOfExecution(int dayOfExecution) {
        this.dayOfExecution = dayOfExecution;
    }
}

