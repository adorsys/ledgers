package de.adorsys.ledgers.deposit.db.domain;

import java.util.Currency;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints={
		@UniqueConstraint(columnNames = { "iban", "currency" }, name = "DepositAccount_iban_currency_unique")
})
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

    
    public DepositAccount(String id, String iban, String msisdn, Currency currency, String name, String product,
                          AccountType accountType, AccountStatus accountStatus, String bic, String linkedAccounts,
                          AccountUsage usageType, String details) {
        super();
        this.id = id;
        this.iban = iban;
        this.msisdn = msisdn;
        this.currency = currency.getCurrencyCode();
        this.name = name;
        this.product = product;
        this.accountType = accountType;
        this.accountStatus = accountStatus;
        this.bic = bic;
        this.linkedAccounts = linkedAccounts;
        this.usageType = usageType;
        this.details = details;
    }

	public DepositAccount() {
	}

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

	public String getMsisdn() {
		return msisdn;
	}

	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
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

	public AccountType getAccountType() {
		return accountType;
	}

	public void setAccountType(AccountType accountType) {
		this.accountType = accountType;
	}

	public AccountStatus getAccountStatus() {
		return accountStatus;
	}

	public void setAccountStatus(AccountStatus accountStatus) {
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

	public AccountUsage getUsageType() {
		return usageType;
	}

	public void setUsageType(AccountUsage usageType) {
		this.usageType = usageType;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}
}
