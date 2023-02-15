/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.api.domain.payment;

import de.adorsys.ledgers.middleware.api.domain.account.AccountReferenceTO;
import de.adorsys.ledgers.middleware.api.domain.general.AddressTO;
import lombok.Data;

import java.util.Currency;
import java.util.List;

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
    private List<String> remittanceInformationUnstructuredArray;
    private List<RemittanceInformationStructuredTO> remittanceInformationStructuredArray;
    private ChargeBearerTO chargeBearerTO;
}
