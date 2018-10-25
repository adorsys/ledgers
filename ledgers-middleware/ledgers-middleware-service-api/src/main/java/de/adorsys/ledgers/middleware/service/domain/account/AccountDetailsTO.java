/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.ledgers.middleware.service.domain.account;

import java.util.Currency;
import java.util.List;


public class AccountDetailsTO {

    private String id; // TODO shouldn't this be resourceId?
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

    public AccountDetailsTO() {
    }

    public AccountDetailsTO(String id, String iban, String bban, String pan, String maskedPan, String msisdn, Currency currency, String name, String product, AccountTypeTO accountType, AccountStatusTO accountStatus, String bic, String linkedAccounts, UsageTypeTO usageType, String details, List<AccountBalanceTO> balances) {
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
        this.balances = balances;
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

    public AccountTypeTO getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountTypeTO accountType) {
        this.accountType = accountType;
    }

    public AccountStatusTO getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(AccountStatusTO accountStatus) {
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

    public UsageTypeTO getUsageType() {
        return usageType;
    }

    public void setUsageType(UsageTypeTO usageType) {
        this.usageType = usageType;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public List<AccountBalanceTO> getBalances() {
        return balances;
    }

    public void setBalances(List<AccountBalanceTO> balances) {
        this.balances = balances;
    }
}
