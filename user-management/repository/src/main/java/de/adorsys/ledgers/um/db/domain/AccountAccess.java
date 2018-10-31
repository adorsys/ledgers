package de.adorsys.ledgers.um.db.domain;

import org.jetbrains.annotations.NotNull;
import javax.persistence.*;

/*
*
*
* */

//Todo clarify unique constrains iban & access_type
@Entity
@Table(name = "account_accesses")
public class AccountAccess {

    @Id
    @Column(name = "account_access_id")
    private String id;


    @NotNull
    @Column(nullable = false)
    private String iban;

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AccessType accessType= AccessType.OWNER;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private UserEntity user;

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public UserEntity getUser() {
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
