/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.deposit.db.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"iban", "currency"}, name = "DepositAccount_iban_currency_unique")
})
@NoArgsConstructor
@AllArgsConstructor
public class DepositAccount {
    @Id
    private String id;

    /*
     * International Bank Account Number
     * 2 letters CountryCode + 2 digits checksum + BBAN
     * DE89 3704 0044 0532 0130 00 (Sample for Germany)
     */
    @Column(nullable = false)
    private String iban;

    /*
     * Mobile Subscriber Integrated Services Digital Number
     * 00499113606980 (Adorsys tel nr)
     */
    private String msisdn;

    @Column(nullable = false)
    private String currency;

    private String name;
    private String displayName;
    private String product;
    private String branch;

    @Enumerated(EnumType.STRING)
    private AccountType accountType;

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
    @Enumerated(EnumType.STRING)
    private AccountUsage usageType;
    /*
     * Specifications that might be provided by the ASPSP
     * - characteristics of the account
     * - characteristics of the relevant card
     */
    private String details;

    /*
     * Is blocked by SYSTEM or STAFF
     */
    @Column(name = "block")
    private boolean blocked;

    /*
     * Is blocked for technical reasons - i.e. deleting some data for this user to prevent data inconsistency
     */
    @Column(name = "systemBlock")
    private boolean systemBlocked;

    @Column
    @CreationTimestamp
    private LocalDateTime created;

    @Column(name = "credit_limit")
    private BigDecimal creditLimit = BigDecimal.ZERO;

    public boolean isEnabled() {
        return !blocked && !systemBlocked;
    }

}
