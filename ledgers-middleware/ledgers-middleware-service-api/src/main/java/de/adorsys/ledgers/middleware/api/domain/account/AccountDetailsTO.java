/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.api.domain.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountDetailsTO {

    private String id;
    /**
     * International Bank Account Number
     * 2 letters CountryCode + 2 digits checksum + BBAN
     * DE89 3704 0044 0532 0130 00 (Sample for Germany)
     */
    private String iban;
    /**
     * Basic Bank Account Number
     * 8 symbols bank id + account number
     * 3704 0044 0532 0130 00 (Sample for Germany)
     */
    private String bban;
    /**
     * Primary Account Number
     * 0000 0000 0000 0000 (Example)
     */
    private String pan;

    /**
     * Same as previous, several signs are masked with "*"
     */
    private String maskedPan;

    /**
     * Mobile Subscriber Integrated Services Digital Number
     * 00499113606980 (Adorsys tel nr)
     */
    private String msisdn;
    private Currency currency;
    private String name;
    private String displayName;
    private String product;
    private AccountTypeTO accountType;
    private AccountStatusTO accountStatus;

    /**
     * SWIFT
     * 4 letters bankCode + 2 letters CountryCode + 2 symbols CityCode + 3 symbols BranchCode
     * DEUTDE8EXXX (Deuche Bank AG example)
     */
    private String bic;
    private String linkedAccounts;
    private UsageTypeTO usageType;
    private String details;

    private List<AccountBalanceTO> balances;

    private boolean blocked;
    private boolean systemBlocked;

    private BigDecimal creditLimit = BigDecimal.ZERO;

    private String branch;

    @JsonIgnore
    public boolean isEnabled() {
        return !blocked && !systemBlocked;
    }
}
