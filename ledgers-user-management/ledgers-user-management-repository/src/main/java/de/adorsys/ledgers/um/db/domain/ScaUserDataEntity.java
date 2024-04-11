/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.um.db.domain;

import de.adorsys.ledgers.util.Ids;
import lombok.Data;

import jakarta.persistence.*;

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

    @Column(nullable = false)
    private boolean valid;

    @OneToOne(mappedBy = "scaUserData")
    private EmailVerificationEntity emailVerification;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = Ids.id();
        }
    }
}
