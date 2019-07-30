package de.adorsys.ledgers.um.db.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Setter
@Getter
@NoArgsConstructor
@Entity(name = "reset_password")
public class ResetPasswordEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "reset_password_generator")
    @SequenceGenerator(name = "reset_password_generator", sequenceName = "reset_password_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "expiry_time", nullable = false)
    private OffsetDateTime expiryTime;


    public ResetPasswordEntity(String userId, String code, OffsetDateTime expiryTime) {
        this.userId = userId;
        this.code = code;
        this.expiryTime = expiryTime;
    }

    public boolean isExpired() {
        return expiryTime.isBefore(OffsetDateTime.now());
    }
}
