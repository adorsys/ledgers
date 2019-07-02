package de.adorsys.ledgers.deposit.api.domain;

import lombok.*;

@Data
@ToString(exclude = {"payment"})
@EqualsAndHashCode(exclude = "payment")
@NoArgsConstructor
@AllArgsConstructor
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
}
