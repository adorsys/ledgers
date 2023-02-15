/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.deposit.api.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum AccountTypeBO {
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

    private static final Map<String, AccountTypeBO> container = new HashMap<>();
    private String value;

    AccountTypeBO(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public static Optional<AccountTypeBO> getByValue(String value) {
        return Optional.ofNullable(container.get(value));
    }

    static {
        AccountTypeBO[] var0 = values();

        for (AccountTypeBO accountType : var0) {
            container.put(accountType.getValue(), accountType);
        }
    }
}
