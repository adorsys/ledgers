package de.adorsys.ledgers.um.db.domain;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@EqualsAndHashCode
@Table(name = "email_verification")
public class EmailVerificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "email_verification_generator")
    @SequenceGenerator(name = "email_verification_generator", sequenceName = "email_verification_id_seq", allocationSize = 1)
    @Column(name = "token_id")
    private Long id;

    @Column(name = "token", unique = true, nullable = false)
    private String token;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private EmailVerificationStatus status;

    @Column(name = "expired_date_time", nullable = false)
    private LocalDateTime expiredDateTime;

    @Column(name = "issued_date_time", nullable = false)
    private LocalDateTime issuedDateTime;

    @Column(name = "confirmed_date_time")
    private LocalDateTime confirmedDateTime;

    @OneToOne
    @JoinColumn(name = "sca_id")
    private ScaUserDataEntity scaUserData;
}