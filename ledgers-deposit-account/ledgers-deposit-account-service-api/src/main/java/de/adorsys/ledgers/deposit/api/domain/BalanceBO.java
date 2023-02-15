/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.deposit.api.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.BinaryOperator;


@Data
public class BalanceBO {
    private AmountBO amount;
    private BalanceTypeBO balanceType;
    private LocalDateTime lastChangeDateTime;
    private LocalDate referenceDate;
    private String lastCommittedTransaction;

    public void updateAmount(BigDecimal amount, BinaryOperator<BigDecimal> functionToApply) {
        BigDecimal newAmount = functionToApply.apply(this.amount.getAmount(), amount);
        this.amount.setAmount(newAmount);
    }

    public boolean isSufficientAmountAvailable(BigDecimal requestedAmount, BigDecimal creditLimit) {
        BigDecimal availableFunds = amount.getAmount().add(creditLimit);
        return Optional.ofNullable(requestedAmount)
                       .map(r -> availableFunds.compareTo(requestedAmount) >= 0)
                       .orElse(false);
    }
}
