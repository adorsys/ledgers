/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.um.db.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

@Setter
@Getter
@NoArgsConstructor
@Entity(name = "oauth_code")
public class OauthCodeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "oauth_code_generator")
    @SequenceGenerator(name = "oauth_code_generator", sequenceName = "oauth_code_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "expiry_time", nullable = false)
    private OffsetDateTime expiryTime;

    @Column(name = "is_used", nullable = false)
    private boolean used;

    @Lob
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "token", nullable = false)
    private String token;

    @Column(name = "final_stage")
    private boolean finalStage;

    public OauthCodeEntity(String userId, String code, OffsetDateTime expiryTime, String token, boolean finalStage) {
        this.userId = userId;
        this.code = code;
        this.expiryTime = expiryTime;
        this.token = token;
        this.finalStage = finalStage;
    }

    public boolean isExpired() {
        return expiryTime.isBefore(OffsetDateTime.now());
    }
}
