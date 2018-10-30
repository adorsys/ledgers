package de.adorsys.ledgers.deposit.api.domain;

public class PaymentTargetBO {
    private String paymentId;
    private String endToEndIdentification;
    private AmountBO instructedAmount;
    private AccountReferenceBO creditorAccount;
    private String creditorAgent;
    private String creditorName;
    private AddressBO creditorAddress;
    private String remittanceInformationUnstructured;
    private PaymentProductBO paymentProduct;
    private PaymentBO payment;

    public PaymentTargetBO() {
    }

    public PaymentTargetBO(String paymentId, String endToEndIdentification, AmountBO instructedAmount, AccountReferenceBO creditorAccount, String creditorAgent, String creditorName, AddressBO creditorAddress, String remittanceInformationUnstructured, PaymentProductBO paymentProduct, PaymentBO payment) {
        this.paymentId = paymentId;
        this.endToEndIdentification = endToEndIdentification;
        this.instructedAmount = instructedAmount;
        this.creditorAccount = creditorAccount;
        this.creditorAgent = creditorAgent;
        this.creditorName = creditorName;
        this.creditorAddress = creditorAddress;
        this.remittanceInformationUnstructured = remittanceInformationUnstructured;
        this.paymentProduct = paymentProduct;
        this.payment = payment;
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

    public AmountBO getInstructedAmount() {
        return instructedAmount;
    }

    public void setInstructedAmount(AmountBO instructedAmount) {
        this.instructedAmount = instructedAmount;
    }

    public AccountReferenceBO getCreditorAccount() {
        return creditorAccount;
    }

    public void setCreditorAccount(AccountReferenceBO creditorAccount) {
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

    public AddressBO getCreditorAddress() {
        return creditorAddress;
    }

    public void setCreditorAddress(AddressBO creditorAddress) {
        this.creditorAddress = creditorAddress;
    }

    public String getRemittanceInformationUnstructured() {
        return remittanceInformationUnstructured;
    }

    public void setRemittanceInformationUnstructured(String remittanceInformationUnstructured) {
        this.remittanceInformationUnstructured = remittanceInformationUnstructured;
    }

    public PaymentProductBO getPaymentProduct() {
        return paymentProduct;
    }

    public void setPaymentProduct(PaymentProductBO paymentProduct) {
        this.paymentProduct = paymentProduct;
    }

    public PaymentBO getPayment() {
        return payment;
    }

    public void setPayment(PaymentBO payment) {
        this.payment = payment;
    }
}
