/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.api.domain.account;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import de.adorsys.ledgers.middleware.api.domain.payment.AmountTO;
import de.adorsys.ledgers.middleware.api.domain.payment.RemittanceInformationStructuredTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionTO {
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
    private AmountTO amount;
    private List<ExchangeRateTO> exchangeRate;
    private String creditorName;
    private AccountReferenceTO creditorAccount;
    private String creditorAgent;
    private String ultimateCreditor;
    private String debtorName;
    private AccountReferenceTO debtorAccount;
    private String debtorAgent;
    private String ultimateDebtor;
    private String additionalInformation;
    private List<String> remittanceInformationUnstructuredArray;
    private List<RemittanceInformationStructuredTO> remittanceInformationStructuredArray;
    private String purposeCode;
    private String bankTransactionCode;
    private String proprietaryBankTransactionCode;
    private AccountBalanceTO balanceAfterTransaction;
}
