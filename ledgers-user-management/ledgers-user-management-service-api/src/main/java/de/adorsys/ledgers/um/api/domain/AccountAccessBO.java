package de.adorsys.ledgers.um.api.domain;

import org.jetbrains.annotations.NotNull;

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

    public int getScaWeight() {
        return scaWeight;
    }

    public void setScaWeight(int scaWeight) {
        this.scaWeight = scaWeight;
    }
}
