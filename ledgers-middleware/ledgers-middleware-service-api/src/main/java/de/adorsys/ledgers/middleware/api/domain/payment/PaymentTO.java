package de.adorsys.ledgers.middleware.api.domain.payment;

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
    private LocalDate requestedExecutionDate;
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
