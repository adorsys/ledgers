package de.adorsys.ledgers.deposit.api.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;

@Data
public class MockBookingDetails {
    private String userAccount;
    private LocalDate bookingDate;
    private LocalDate valueDate;
    private String remittance;
    private String crDrName;
    private String otherAccount;
    private BigDecimal amount;
    private Currency currency;

    @JsonIgnore
    public boolean isPaymentTransaction() {
        return amount.compareTo(BigDecimal.ZERO) < 0;
    }
}
