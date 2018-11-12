package de.adorsys.ledgers.um.db.domain;

import org.hibernate.annotations.GenericGenerator;
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
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;


    @NotNull
    @Column(nullable = false)
    private String iban;

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AccessType accessType= AccessType.OWNER;

    public String getId() {
        return id;
    }

    public String getIban() {
        return iban;
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
