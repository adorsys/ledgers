/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.deposit.db.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;

public enum AccountType {
    CACC("Current"),
    CASH("CashPayment"),
    CHAR("Charges"),
    CISH("CashIncome"),
    COMM("Commission"),
    CPAC("ClearingParticipantSettlementAccount"),
    LLSV("LimitedLiquiditySavingsAccount"),
    LOAN("Loan"),
    MGLD("Marginal Lending"),
    MOMA("Money Market"),
    NREX("NonResidentExternal"),
    ODFT("Overdraft"),
    ONDP("OverNightDeposit"),
    OTHR("OtherAccount"),
    SACC("Settlement"),
    SLRY("Salary"),
    SVGS("Savings"),
    TAXE("Tax"),
    TRAN("TransactingAccount"),
    TRAS("Cash Trading");

    private static final Map<String, AccountType> container = new HashMap<>();
    private String value;

    @JsonCreator
    AccountType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    @JsonIgnore
    public static Optional<AccountType> getByValue(String value) {
        return Optional.ofNullable(container.get(value));
    }

    static {
        AccountType[] var0 = values();

        for (AccountType accountType : var0) {
            container.put(accountType.getValue(), accountType);
        }
    }
}
