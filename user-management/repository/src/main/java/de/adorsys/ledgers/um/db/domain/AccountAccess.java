package de.adorsys.ledgers.um.db.domain;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.jetbrains.annotations.NotNull;

import javax.persistence.*;

//Todo clearify unique constrains iban & access_type
@Entity
@Table(name = "account_accesses", uniqueConstraints = {
        @UniqueConstraint(columnNames = "iban", name = "iban_unique"),
        @UniqueConstraint(columnNames = "access_type", name = "access_type_unique")
})
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

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name= "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
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
