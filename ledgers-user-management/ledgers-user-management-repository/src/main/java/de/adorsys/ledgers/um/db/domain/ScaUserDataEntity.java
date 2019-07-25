package de.adorsys.ledgers.um.db.domain;

import de.adorsys.ledgers.util.Ids;
import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "sca_data")
@Data
public class ScaUserDataEntity {

    @Id
    @Column(name = "sca_id")
    private String id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ScaMethodType scaMethod;

    @Column(nullable = false)
    private String methodValue;

    @Column(nullable = false)
    private boolean usesStaticTan;

    @Column
    private String staticTan;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = Ids.id();
        }
    }
}
