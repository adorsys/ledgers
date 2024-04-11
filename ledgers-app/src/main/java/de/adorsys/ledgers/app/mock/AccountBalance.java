/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.app.mock;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Currency;

@Data
public class AccountBalance {
    private BigDecimal balance;
    private String accNbr;
    private Currency currency;
}
