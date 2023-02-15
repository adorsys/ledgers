/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.api.domain.payment;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import de.adorsys.ledgers.middleware.api.domain.account.AccountReferenceTO;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class PaymentTO {
    private String paymentId;
    private Boolean batchBookingPreferred;
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate requestedExecutionDate;
    @JsonDeserialize(using = LocalTimeDeserializer.class)
    @JsonSerialize(using = LocalTimeSerializer.class)
    private LocalTime requestedExecutionTime;
    private PaymentTypeTO paymentType;
    private String paymentProduct; //represents serviceLevel in SEPA as code value not a regular string representation sepa-credit-transfers
    private LocalDate startDate;
    private LocalDate endDate;
    private String executionRule;
    private FrequencyCodeTO frequency;
    private Integer dayOfExecution; //Day here max 31
    private AccountReferenceTO debtorAccount;
    private String debtorAgent;
    private String debtorName;
    private TransactionStatusTO transactionStatus;
    private List<PaymentTargetTO> targets = new ArrayList<>();
    private String accountId;
}
