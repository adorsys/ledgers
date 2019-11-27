package de.adorsys.ledgers.deposit.api.domain;

import lombok.Data;

import java.util.List;

@Data
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
    private List<ExchangeRateBO> exchangeRate;
}
