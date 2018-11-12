package de.adorsys.ledgers.deposit.api.domain;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;

public class TransactionDetailsBO {
    private String transactionId;
    private String entryReference;
    private String endToEndId;
    private String mandateId;
    private String checkId;
    private String creditorId;
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate bookingDate;
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate valueDate;
    private AmountBO transactionAmount;
    private List<ExchangeRateBO> exchangeRate;
    private String creditorName;
    private AccountReferenceBO creditorAccount;
    private String ultimateCreditor;
    private String debtorName;
    private AccountReferenceBO debtorAccount;
    private String ultimateDebtor;
    private String remittanceInformationStructured;
    private String remittanceInformationUnstructured;
    private PurposeCodeBO purposeCode;
    private String bankTransactionCode;
    private String proprietaryBankTransactionCode;

    public TransactionDetailsBO() {
    }

    public TransactionDetailsBO(String transactionId, String entryReference, String endToEndId, String mandateId, String checkId, String creditorId, LocalDate bookingDate, LocalDate valueDate, AmountBO transactionAmount, List<ExchangeRateBO> exchangeRate, String creditorName, AccountReferenceBO creditorAccount, String ultimateCreditor, String debtorName, AccountReferenceBO debtorAccount, String ultimateDebtor, String remittanceInformationStructured, String remittanceInformationUnstructured, PurposeCodeBO purposeCode, String bankTransactionCode, String proprietaryBankTransactionCode) {
        this.transactionId = transactionId;
        this.entryReference = entryReference;
        this.endToEndId = endToEndId;
        this.mandateId = mandateId;
        this.checkId = checkId;
        this.creditorId = creditorId;
        this.bookingDate = bookingDate;
        this.valueDate = valueDate;
        this.transactionAmount = transactionAmount;
        this.exchangeRate = exchangeRate;
        this.creditorName = creditorName;
        this.creditorAccount = creditorAccount;
        this.ultimateCreditor = ultimateCreditor;
        this.debtorName = debtorName;
        this.debtorAccount = debtorAccount;
        this.ultimateDebtor = ultimateDebtor;
        this.remittanceInformationStructured = remittanceInformationStructured;
        this.remittanceInformationUnstructured = remittanceInformationUnstructured;
        this.purposeCode = purposeCode;
        this.bankTransactionCode = bankTransactionCode;
        this.proprietaryBankTransactionCode = proprietaryBankTransactionCode;
    }

    //Getters-setters

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getEntryReference() {
        return entryReference;
    }

    public void setEntryReference(String entryReference) {
        this.entryReference = entryReference;
    }

    public String getEndToEndId() {
        return endToEndId;
    }

    public void setEndToEndId(String endToEndId) {
        this.endToEndId = endToEndId;
    }

    public String getMandateId() {
        return mandateId;
    }

    public void setMandateId(String mandateId) {
        this.mandateId = mandateId;
    }

    public String getCheckId() {
        return checkId;
    }

    public void setCheckId(String checkId) {
        this.checkId = checkId;
    }

    public String getCreditorId() {
        return creditorId;
    }

    public void setCreditorId(String creditorId) {
        this.creditorId = creditorId;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public LocalDate getValueDate() {
        return valueDate;
    }

    public void setValueDate(LocalDate valueDate) {
        this.valueDate = valueDate;
    }

    public AmountBO getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(AmountBO transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public List<ExchangeRateBO> getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(List<ExchangeRateBO> exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public String getCreditorName() {
        return creditorName;
    }

    public void setCreditorName(String creditorName) {
        this.creditorName = creditorName;
    }

    public AccountReferenceBO getCreditorAccount() {
        return creditorAccount;
    }

    public void setCreditorAccount(AccountReferenceBO creditorAccount) {
        this.creditorAccount = creditorAccount;
    }

    public String getUltimateCreditor() {
        return ultimateCreditor;
    }

    public void setUltimateCreditor(String ultimateCreditor) {
        this.ultimateCreditor = ultimateCreditor;
    }

    public String getDebtorName() {
        return debtorName;
    }

    public void setDebtorName(String debtorName) {
        this.debtorName = debtorName;
    }

    public AccountReferenceBO getDebtorAccount() {
        return debtorAccount;
    }

    public void setDebtorAccount(AccountReferenceBO debtorAccount) {
        this.debtorAccount = debtorAccount;
    }

    public String getUltimateDebtor() {
        return ultimateDebtor;
    }

    public void setUltimateDebtor(String ultimateDebtor) {
        this.ultimateDebtor = ultimateDebtor;
    }

    public String getRemittanceInformationStructured() {
        return remittanceInformationStructured;
    }

    public void setRemittanceInformationStructured(String remittanceInformationStructured) {
        this.remittanceInformationStructured = remittanceInformationStructured;
    }

    public String getRemittanceInformationUnstructured() {
        return remittanceInformationUnstructured;
    }

    public void setRemittanceInformationUnstructured(String remittanceInformationUnstructured) {
        this.remittanceInformationUnstructured = remittanceInformationUnstructured;
    }

    public PurposeCodeBO getPurposeCode() {
        return purposeCode;
    }

    public void setPurposeCode(PurposeCodeBO purposeCode) {
        this.purposeCode = purposeCode;
    }

    public String getBankTransactionCode() {
        return bankTransactionCode;
    }

    public void setBankTransactionCode(String bankTransactionCode) {
        this.bankTransactionCode = bankTransactionCode;
    }

    public String getProprietaryBankTransactionCode() {
        return proprietaryBankTransactionCode;
    }

    public void setProprietaryBankTransactionCode(String proprietaryBankTransactionCode) {
        this.proprietaryBankTransactionCode = proprietaryBankTransactionCode;
    }
}
