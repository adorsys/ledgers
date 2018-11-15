package de.adorsys.ledgers.deposit.api.domain;

public class PaymentTargetDetailsBO extends TransactionDetailsBO {
    private AddressBO creditorAddress;
    /*Id of the referenced payment*/
    private String paymentOrderId;
    /*The type of the payment order.*/
    private PaymentTypeBO paymentType;
    /*The transaction status*/
    private TransactionStatusBO transactionStatus;
    private PaymentProductBO paymentProduct;
    private String creditorAgent;

    //Getters - Setters
    public AddressBO getCreditorAddress() {
        return creditorAddress;
    }

    public void setCreditorAddress(AddressBO creditorAddress) {
        this.creditorAddress = creditorAddress;
    }

    public String getPaymentOrderId() {
        return paymentOrderId;
    }

    public void setPaymentOrderId(String paymentOrderId) {
        this.paymentOrderId = paymentOrderId;
    }

    public PaymentTypeBO getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentTypeBO paymentType) {
        this.paymentType = paymentType;
    }

    public TransactionStatusBO getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(TransactionStatusBO transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public PaymentProductBO getPaymentProduct() {
        return paymentProduct;
    }

    public void setPaymentProduct(PaymentProductBO paymentProduct) {
        this.paymentProduct = paymentProduct;
    }

    public String getCreditorAgent() {
        return creditorAgent;
    }

    public void setCreditorAgent(String creditorAgent) {
        this.creditorAgent = creditorAgent;
    }


}
