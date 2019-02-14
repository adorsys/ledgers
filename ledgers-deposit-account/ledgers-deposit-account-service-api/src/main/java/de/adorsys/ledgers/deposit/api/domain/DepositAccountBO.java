package de.adorsys.ledgers.deposit.api.domain;

import java.util.Currency;


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
    private String product;

    private AccountTypeBO accountType;

    private AccountStatusBO accountStatus = AccountStatusBO.ENABLED;

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

    public DepositAccountBO() {
    }

    public DepositAccountBO(String id, String iban, String bban, String pan, String maskedPan, String msisdn, Currency currency, String name, String product, AccountTypeBO accountType, AccountStatusBO accountStatus, String bic, String linkedAccounts, AccountUsageBO usageType, String details) {
        this.id = id;
        this.iban = iban;
        this.bban = bban;
        this.pan = pan;
        this.maskedPan = maskedPan;
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

    //Getters-Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public String getBban() {
        return bban;
    }

    public void setBban(String bban) {
        this.bban = bban;
    }

    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public String getMaskedPan() {
        return maskedPan;
    }

    public void setMaskedPan(String maskedPan) {
        this.maskedPan = maskedPan;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public AccountTypeBO getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountTypeBO accountType) {
        this.accountType = accountType;
    }

    public AccountStatusBO getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(AccountStatusBO accountStatus) {
        this.accountStatus = accountStatus;
    }

    public String getBic() {
        return bic;
    }

    public void setBic(String bic) {
        this.bic = bic;
    }

    public String getLinkedAccounts() {
        return linkedAccounts;
    }

    public void setLinkedAccounts(String linkedAccounts) {
        this.linkedAccounts = linkedAccounts;
    }

    public AccountUsageBO getUsageType() {
        return usageType;
    }

    public void setUsageType(AccountUsageBO usageType) {
        this.usageType = usageType;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
