package de.adorsys.ledgers.um.api.domain;

import org.jetbrains.annotations.NotNull;

public class AccountAccessBO {

    private String id;

    @NotNull
    private String iban;

    @NotNull
    private AccessTypeBO accessType;

    @NotNull
    private UserBO user;

    public AccountAccessBO(@NotNull String iban, AccessTypeBO accessType, @NotNull UserBO user) {
        this.iban = iban;
        this.accessType = accessType;
        this.user = user;
    }

    public AccountAccessBO() {
	}


	public void setUser(UserBO user) {
        this.user = user;
    }

    public UserBO getUser() {
        return user;
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

}
