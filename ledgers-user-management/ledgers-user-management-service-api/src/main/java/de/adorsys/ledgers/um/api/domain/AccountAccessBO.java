package de.adorsys.ledgers.um.api.domain;

import org.jetbrains.annotations.NotNull;

public class AccountAccessBO {

    private String id;

    @NotNull
    private String iban;

    @NotNull
    private AccessTypeBO accessType;

    private int scaRequiredToAuthorise;

    public AccountAccessBO(@NotNull String iban, AccessTypeBO accessType) {
        this.iban = iban;
        this.accessType = accessType;
    }

    public AccountAccessBO() {
	}

    public String getId() {
        return id;
    }

    public String getIban() {
        return iban;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public AccessTypeBO getAccessType() {
        return accessType;
    }

    public void setAccessType(AccessTypeBO accessType) {
        this.accessType = accessType;
    }

    public int getScaRequiredToAuthorise() {
        return scaRequiredToAuthorise;
    }

    public void setScaRequiredToAuthorise(int scaRequiredToAuthorise) {
        this.scaRequiredToAuthorise = scaRequiredToAuthorise;
    }
}
