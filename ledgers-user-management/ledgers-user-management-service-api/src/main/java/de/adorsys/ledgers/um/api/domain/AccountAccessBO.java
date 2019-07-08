package de.adorsys.ledgers.um.api.domain;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class AccountAccessBO {
    private String id;
    @NotNull
    private String iban;
    @NotNull
    private AccessTypeBO accessType;
    @NotNull
    private int scaWeight;

    public AccountAccessBO(@NotNull String iban, AccessTypeBO accessType) {
        this.iban = iban;
        this.accessType = accessType;
    }

    public AccountAccessBO() {}
}
