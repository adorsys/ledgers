package de.adorsys.ledgers.deposit.db.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters.LocalDateConverter;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class Payment {
    /*
     * The is id of the payment request
     */
    @Id
    private String paymentId;

    /*
     * If this element equals "true", the PSU prefers only one booking entry. If
     * this element equals "false", the PSU prefers individual booking of all
     * contained individual transactions. The ASPSP will follow this preference
     * according to contracts agreed on with the PSU.
     *
     * This is only used for payments of type de.adorsys.ledgers.deposit.domain.PaymentTypeBO.BULK
     */
    private Boolean batchBookingPreferred;

    /**
     * Is used for any kind of payments except Periodic
     */
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @Convert(converter = LocalDateConverter.class)
    private LocalDate requestedExecutionDate;

    /**
     * Is used for regular payments when User is eager to have them executed at certain time (not before)
     */
    //@Convert(converter = LocalTimeConverter.class)
    private LocalTime requestedExecutionTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentType paymentType;

    private String paymentProduct;

    /**
     * Represents the starting date for Periodic payments
     */
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @Convert(converter = LocalDateConverter.class)
    private LocalDate startDate;

    /**
     * Represents the latest possible date for Periodic payments
     */
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @Convert(converter = LocalDateConverter.class)
    private LocalDate endDate;

    /**
     * String representation of proceeding / following indication for pay day. Example: requested ExecutionDate is set to 15.12.2018 and that is a Saturday
     * So the payment should be executed either 14.12.2018 or 17.12.2018 (Friday or Monday)
     */
    private String executionRule;

    /**
     * Represents the frequency for Periodic payment (weekly/two weeks/monthly etc.)
     */
    @Enumerated(EnumType.STRING)
    private FrequencyCode frequency; // TODO consider using an enum similar to FrequencyCode based on the the "EventFrequency7Code" of ISO 20022

    /**
     * Day of execution for Periodic Payments if it is necessary to execute a payment on certain dates
     */
    private Integer dayOfExecution; //Day here max 31

    /**
     * Last execution date (when the payment was executed at last)
     */
    private LocalDateTime executedDate;

    /**
     * Date when the Execution Scheduler will pick the payment for execution (Should be null if the payment is executed for the last time)
     */
    private LocalDateTime nextScheduledExecution;

    @NotNull
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "iban", column = @Column(name = "DEBT_IBAN")),
            @AttributeOverride(name = "bban", column = @Column(name = "DEBT_BBAN")),
            @AttributeOverride(name = "pan", column = @Column(name = "DEBT_PAN")),
            @AttributeOverride(name = "maskedPan", column = @Column(name = "DEBT_MASKED_PAN")),
            @AttributeOverride(name = "msisdn", column = @Column(name = "DEBT_MSISDN"))
    })
    @Column(nullable = false)
    private AccountReference debtorAccount;

    private String debtorName;

    private String debtorAgent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus transactionStatus;

    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.ALL})
    private List<PaymentTarget> targets = new ArrayList<>();

    @Column(nullable = false)
    private String accountId;

    public boolean isLastExecuted(LocalDate nextPossibleExecutionDate){
        return endDate != null && nextPossibleExecutionDate.isAfter(endDate);
    }
}
