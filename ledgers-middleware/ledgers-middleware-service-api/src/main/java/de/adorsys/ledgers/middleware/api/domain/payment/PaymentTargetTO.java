package de.adorsys.ledgers.middleware.api.domain.payment;

import de.adorsys.ledgers.middleware.api.domain.account.AccountReferenceTO;
import de.adorsys.ledgers.middleware.api.domain.general.AddressTO;
import lombok.Data;

import java.util.Currency;

@Data
public class PaymentTargetTO {
    private String paymentId;
    private String endToEndIdentification;
    private AmountTO instructedAmount;
    private Currency currencyOfTransfer;
    private AccountReferenceTO creditorAccount;
    private String creditorAgent;
    private String creditorName;
    private AddressTO creditorAddress;
    private PurposeCodeTO purposeCode;
    private String remittanceInformationUnstructured;
    private RemittanceInformationStructuredTO remittanceInformationStructured;
}
