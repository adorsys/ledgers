package de.adorsys.ledgers.um.db.domain;

import de.adorsys.ledgers.util.Ids;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import javax.persistence.*;
import java.time.LocalDateTime;

//Todo clarify unique constrains iban & access_type
@Entity
@Data
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
    private String currency;

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AccessType accessType = AccessType.OWNER;

    @Column(nullable = false)
    private int scaWeight;

    @Column(nullable = false)
    private String accountId;

    @Column
    private LocalDateTime created = LocalDateTime.now();

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = Ids.id();
        }
    }
}
