package de.adorsys.ledgers.deposit.api.domain;

import lombok.*;

import java.util.Currency;
import java.util.Optional;

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

    public boolean isAllCurrenciesMatch() {
        Currency amount = instructedAmount.getCurrency();
        boolean debtor = amount.equals(Optional.ofNullable(payment)
                                               .map(PaymentBO::getDebtorAccount)
                                               .map(AccountReferenceBO::getCurrency)
                                               .orElse(null));
        return debtor && amount.equals(creditorAccount.getCurrency());
    }
}
