package de.adorsys.ledgers.um.api.domain;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.Currency;

@Data
public class AccountAccessBO {
    private String id;
    private String iban;
    private Currency currency;
    private AccessTypeBO accessType;
    private int scaWeight;
    private String accountId;
    private LocalDateTime created;

    public AccountAccessBO(@NotNull String iban, AccessTypeBO accessType) {
        this.iban = iban;
        this.accessType = accessType;
    }

    public AccountAccessBO() {
    }

    public void updateAccessFields(String iban, Currency currency) {
        this.setIban(iban);
        this.setCurrency(currency);
    }
}