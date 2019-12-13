package de.adorsys.ledgers.middleware.api.domain.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * @deprecated Shall be removed in v2.5
 */
@Deprecated
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PeriodicPaymentTO extends SinglePaymentTO {
    private LocalDate startDate;
    private LocalDate endDate;
    private String executionRule;
    private FrequencyCodeTO frequency;
    private Integer dayOfExecution;
}

