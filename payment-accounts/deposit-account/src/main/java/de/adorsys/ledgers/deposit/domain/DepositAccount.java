package de.adorsys.ledgers.deposit.domain;

import java.util.Currency;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
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
    private Currency currency;
    private String name;
    private String product;
    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    private AccountStatus accountStatus = AccountStatus.ENABLED;

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

    @Builder
    public DepositAccount(String id, String iban, String msisdn, Currency currency, String name, String product,
                          AccountType accountType, AccountStatus accountStatus, String bic, String linkedAccounts,
                          AccountUsage usageType, String details) {
        super();
        this.id = id;
        this.iban = iban;
        this.msisdn = msisdn;
        this.currency = currency;
        this.name = name;
        this.product = product;
        this.accountType = accountType;
        this.accountStatus = accountStatus;
        this.bic = bic;
        this.linkedAccounts = linkedAccounts;
        this.usageType = usageType;
        this.details = details;
    }
}
