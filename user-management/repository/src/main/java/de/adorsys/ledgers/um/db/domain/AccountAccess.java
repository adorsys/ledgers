package de.adorsys.ledgers.um.db.domain;

import org.jetbrains.annotations.NotNull;

import javax.persistence.Column;
import javax.persistence.Id;

public class AccountAccess {

    @Id
    @Column(name = "account_access_id")
    private String id;

    @NotNull
    @Column(nullable = false)
    private String iban;

    @NotNull
    @Column(nullable = false)
    private String accessType;



    public String getId() {
        return id;
    }

    public String getIban() {
        return iban;
    }

    public String getAccessType() {
        return accessType;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public void setAccessType(String accessType) {
        this.accessType = accessType;
    }
}
