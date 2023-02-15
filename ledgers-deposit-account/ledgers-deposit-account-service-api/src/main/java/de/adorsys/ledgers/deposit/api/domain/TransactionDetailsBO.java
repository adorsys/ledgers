/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.deposit.api.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.adorsys.ledgers.deposit.api.service.util.BytesToStringDeserializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionDetailsBO {
    private String transactionId;
    private String entryReference;
    private String endToEndId;
    private String mandateId;
    private String checkId;
    private String creditorId;
    private LocalDate bookingDate;
    private LocalDate valueDate;
    private AmountBO transactionAmount;
    private List<ExchangeRateBO> exchangeRate;
    private String creditorName;
    private String creditorAgent;
    private AccountReferenceBO creditorAccount;
    private String ultimateCreditor;
    private String debtorName;
    private String debtorAgent;
    private AccountReferenceBO debtorAccount;
    private String ultimateDebtor;
    private String additionalInformation;

    @JsonDeserialize(using = BytesToStringDeserializer.class)
    private byte[] remittanceInformationStructuredArray;

    @JsonDeserialize(using = BytesToStringDeserializer.class)
    private byte[] remittanceInformationUnstructuredArray;
    private PurposeCodeBO purposeCode;
    private String bankTransactionCode;
    private String proprietaryBankTransactionCode;
    private BalanceBO balanceAfterTransaction;
}
