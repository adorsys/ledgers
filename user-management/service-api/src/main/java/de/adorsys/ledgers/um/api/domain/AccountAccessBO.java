package de.adorsys.ledgers.um.api.domain;

import de.adorsys.ledgers.um.db.domain.AccessType;
import org.jetbrains.annotations.NotNull;

public class AccountAccessBO {

    private String id;

    @NotNull
    private String iban;

    @NotNull
    private AccessType accessType= AccessType.OWNER;

    @NotNull
    private UserBO user;

    public AccountAccessBO(@NotNull String iban, AccessType accessType, @NotNull UserBO user) {
        this.iban = iban;
        this.accessType = accessType;
        this.user = user;
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

    public AccessType getAccessType() {
        return accessType;
    }

    public void setAccessType(AccessType accessType) {
        this.accessType = accessType;
    }

}
