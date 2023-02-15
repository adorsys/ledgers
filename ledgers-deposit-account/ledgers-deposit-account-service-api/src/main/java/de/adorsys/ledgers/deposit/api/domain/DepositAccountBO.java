/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.deposit.api.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositAccountBO {

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

    private AccountTypeBO accountType;

    /*
     * SWIFT
     * 4 letters bankCode + 2 letters CountryCode + 2 symbols CityCode + 3 symbols BranchCode
     * DEUTDE8EXXX (Deuche Bank AG example)
     */
    private String bic;
    private String linkedAccounts;
    /*
     * This defines whether the account is owned by an organization or by a private person.
     *
     * This might have a impact on the account access permissions are managed.
     *
     * This correspond the XS2A {@link SpiUsageType}
     *
     * @author fpo
     *
     */

    private AccountUsageBO usageType;
    /*
     * Specifications that might be provided by the ASPSP
     * - characteristics of the account
     * - characteristics of the relevant card
     */
    private String details;

    private boolean blocked;

    private boolean systemBlocked;

    private String branch;

    private LocalDateTime created;

    private BigDecimal creditLimit = BigDecimal.ZERO;

    public boolean isEnabled() {
        return !blocked && !systemBlocked;
    }

    public AccountReferenceBO getReference() {
        AccountReferenceBO reference = new AccountReferenceBO();
        reference.setIban(iban);
        reference.setCurrency(currency);
        reference.setBban(bban);
        reference.setMsisdn(msisdn);
        reference.setPan(pan);
        reference.setMaskedPan(maskedPan);
        return reference;
    }
}
