package de.adorsys.ledgers.middleware.api.domain.payment;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;

import de.adorsys.ledgers.middleware.api.domain.account.AccountReferenceTO;
import de.adorsys.ledgers.middleware.api.domain.general.AddressTO;

public class SinglePaymentTO {
    private String paymentId;
    private String endToEndIdentification;
    private AccountReferenceTO debtorAccount;
    private AmountTO instructedAmount;
    private AccountReferenceTO creditorAccount;
    private String creditorAgent;
    private String creditorName;
    private AddressTO creditorAddress;
    private String remittanceInformationUnstructured;
    private TransactionStatusTO paymentStatus;
    private PaymentProductTO paymentProduct;
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate requestedExecutionDate;
    @JsonDeserialize(using = LocalTimeDeserializer.class)
    @JsonSerialize(using = LocalTimeSerializer.class)
    private LocalTime requestedExecutionTime;

    public SinglePaymentTO() {
    }

    public SinglePaymentTO(String paymentId, String endToEndIdentification, AccountReferenceTO debtorAccount, AmountTO instructedAmount, AccountReferenceTO creditorAccount, String creditorAgent, String creditorName, AddressTO creditorAddress, String remittanceInformationUnstructured, TransactionStatusTO paymentStatus, PaymentProductTO paymentProduct, LocalDate requestedExecutionDate, LocalTime requestedExecutionTime) {
        this.paymentId = paymentId;
        this.endToEndIdentification = endToEndIdentification;
        this.debtorAccount = debtorAccount;
        this.instructedAmount = instructedAmount;
        this.creditorAccount = creditorAccount;
        this.creditorAgent = creditorAgent;
        this.creditorName = creditorName;
        this.creditorAddress = creditorAddress;
        this.remittanceInformationUnstructured = remittanceInformationUnstructured;
        this.paymentStatus = paymentStatus;
        this.paymentProduct = paymentProduct;
        this.requestedExecutionDate = requestedExecutionDate;
        this.requestedExecutionTime = requestedExecutionTime;
    }

    //Getters-Setters
    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getEndToEndIdentification() {
        return endToEndIdentification;
    }

    public void setEndToEndIdentification(String endToEndIdentification) {
        this.endToEndIdentification = endToEndIdentification;
    }

    public AccountReferenceTO getDebtorAccount() {
        return debtorAccount;
    }

    public void setDebtorAccount(AccountReferenceTO debtorAccount) {
        this.debtorAccount = debtorAccount;
    }

    public AmountTO getInstructedAmount() {
        return instructedAmount;
    }

    public void setInstructedAmount(AmountTO instructedAmount) {
        this.instructedAmount = instructedAmount;
    }

    public AccountReferenceTO getCreditorAccount() {
        return creditorAccount;
    }

    public void setCreditorAccount(AccountReferenceTO creditorAccount) {
        this.creditorAccount = creditorAccount;
    }

    public String getCreditorAgent() {
        return creditorAgent;
    }

    public void setCreditorAgent(String creditorAgent) {
        this.creditorAgent = creditorAgent;
    }

    public String getCreditorName() {
        return creditorName;
    }

    public void setCreditorName(String creditorName) {
        this.creditorName = creditorName;
    }

    public AddressTO getCreditorAddress() {
        return creditorAddress;
    }

    public void setCreditorAddress(AddressTO creditorAddress) {
        this.creditorAddress = creditorAddress;
    }

    public String getRemittanceInformationUnstructured() {
        return remittanceInformationUnstructured;
    }

    public void setRemittanceInformationUnstructured(String remittanceInformationUnstructured) {
        this.remittanceInformationUnstructured = remittanceInformationUnstructured;
    }

    public TransactionStatusTO getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(TransactionStatusTO paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public PaymentProductTO getPaymentProduct() {
        return paymentProduct;
    }

    public void setPaymentProduct(PaymentProductTO paymentProduct) {
        this.paymentProduct = paymentProduct;
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
}
