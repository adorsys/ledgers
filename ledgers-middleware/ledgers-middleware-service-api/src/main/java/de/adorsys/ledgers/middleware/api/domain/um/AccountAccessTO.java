package de.adorsys.ledgers.middleware.api.domain.um;

import org.jetbrains.annotations.NotNull;

public class AccountAccessTO {

    private String id;

    @NotNull
    private String iban;

    @NotNull
    private AccessTypeTO accessType;

    @NotNull
    private UserTO user;

    public AccountAccessTO(@NotNull String iban, AccessTypeTO accessType, @NotNull UserTO user) {
        this.iban = iban;
        this.accessType = accessType;
        this.user = user;
    }

    public AccountAccessTO() {
	}


	public void setUser(UserTO user) {
        this.user = user;
    }

    public UserTO getUser() {
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

    public AccessTypeTO getAccessType() {
        return accessType;
    }

    public void setAccessType(AccessTypeTO accessType) {
        this.accessType = accessType;
    }

}
