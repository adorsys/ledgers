package de.adorsys.ledgers.data.upload.model;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Currency;

public class AccountBalance {
    private String accountId;
    @NotNull
    private String iban;
    @NotNull
    private Currency currency;
    @NotNull
    private BigDecimal amount;

    public AccountBalance() {
    }

    public AccountBalance(String accountId, String iban, Currency currency, BigDecimal amount) {
        this.accountId = accountId;
        this.iban = iban;
        this.currency = currency;
        this.amount = amount;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
