/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.sca.db.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters.LocalDateTimeConverter;

import java.time.LocalDateTime;
import java.util.EnumSet;

import static de.adorsys.ledgers.sca.db.domain.AuthCodeStatus.*;

/**
 * The SCA operation entity. We distinguish among following business operations.
 * - Login : - the opId shall be a generated login session id. - the
 * authorisationId shall be the same as the operation id. - Consent: A single
 * consent might need multiple authorisations. - the opId is the consentId - the
 * authorisationId is a generated number. - Payment: A single payment might need
 * multiple authorisations. - the opId is the paymentId - the authorisationId is
 * a generated number.
 * <p>
 * We can create an SCA without have sent out the code. This is generally the
 * case when a business operation (login, consent, payment) is initiated.
 *
 * @author fpo
 */
@Entity
@Data
@Table(name = "sca_operation")
@NoArgsConstructor
public class SCAOperationEntity {

    /**
     * The id of this authorization instance. This will generally match the
     * authorization id.
     */
    @Id
    private String id;

    /**
     * The id of the business operation being authorized.
     */
    @Column(name = "op_id", nullable = false, updatable = false)
    private String opId;

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "validity_seconds", nullable = false, updatable = false)
    private int validitySeconds;

    /* The hash of auth code and opData */
    @Column(name = "auth_code_hash")
    private String authCodeHash;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AuthCodeStatus status;

    @Column(name = "hash_alg")
    private String hashAlg;

    @Column(nullable = false, updatable = false)
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime created;

    @Column(name = "status_time", nullable = false)
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime statusTime;

    /* The operation type. Could be login, consent, payment. */
    @Column(name = "op_type")
    @Enumerated(EnumType.STRING)
    private OpType opType;

    /*
     * Stores the sca method is associated with this authorization. The sca method
     * id is not supposed to change after the code was sent out.
     */
    @Column(name = "sca_method_id")
    private String scaMethodId;

    /*
     * Records the number of failed attempts.
     *
     */
    @Column(name = "failled_count")
    private int failledCount;

    @Column(name = "sca_status")
    @Enumerated(EnumType.STRING)
    private ScaStatus scaStatus;

    @Column(nullable = false)
    private int scaWeight;

    @Column
    @UpdateTimestamp
    private LocalDateTime updated;

    @PrePersist
    public void prePersist() {
        if (created == null) {
            created = LocalDateTime.now();
        }
    }

    public SCAOperationEntity updateStatuses(boolean isCodeConfirmValid) {
        this.status = VALIDATED;
        this.scaStatus = ScaStatus.FINALISED;
        if (!isCodeConfirmValid) {
            this.status = FAILED;
            this.scaStatus = ScaStatus.FAILED;
        }
        this.statusTime = LocalDateTime.now();
        return this;
    }

    public boolean isOperationExpired() {
        boolean hasExpiredStatus = this.status == EXPIRED;
        return hasExpiredStatus || LocalDateTime.now().isAfter(this.created.plusSeconds(this.validitySeconds));
    }

    public void validate(ScaStatus scaStatus, int scaWeight) {
        this.scaStatus = scaStatus;
        this.scaWeight = scaWeight;
        updateStatusAndTime(VALIDATED);
    }

    public void expireOperation() {
        this.scaStatus = ScaStatus.FAILED;
        this.scaWeight = 0;
        updateStatusAndTime(EXPIRED);
    }

    public boolean isOperationAlreadyUsed() {
        return EnumSet.of(VALIDATED, EXPIRED, DONE).contains(this.status)
                       || EnumSet.of(ScaStatus.FAILED, ScaStatus.FINALISED).contains(this.scaStatus);
    }

    public SCAOperationEntity(String authId, String opId, String externalId, OpType opType, String scaMethodId, int validitySeconds, int authCodeValiditySeconds, ScaStatus scaStatus, int scaWeight) {
        this.id = authId;
        this.opId = opId;
        this.externalId = externalId;
        this.opType = opType;
        this.scaMethodId = scaMethodId;
        this.created = LocalDateTime.now();
        this.validitySeconds = validitySeconds <= 0
                                       ? authCodeValiditySeconds
                                       : validitySeconds;
        this.scaStatus = scaStatus;
        this.scaWeight = scaWeight;
        updateStatusAndTime(INITIATED);
    }

    public void updateStatusSent(int authCodeValiditySeconds, String authCodeHash, String hashAlg) {
        this.created = LocalDateTime.now();
        this.validitySeconds = this.validitySeconds <= 0
                                       ? authCodeValiditySeconds
                                       : this.validitySeconds;
        this.hashAlg = hashAlg;
        this.authCodeHash = authCodeHash;
        updateStatusAndTime(SENT);
    }

    public void fail(boolean isLoginOperation, int loginFailedMax, int authCodeFailedMax) {
        this.failledCount = this.failledCount + 1;
        int failedMax = isLoginOperation
                                ? loginFailedMax
                                : authCodeFailedMax;
        if (this.failledCount >= failedMax) {
            this.scaStatus = ScaStatus.FAILED;
        }
        updateStatusAndTime(FAILED);
    }

    private void updateStatusAndTime(AuthCodeStatus status) {
        this.statusTime = LocalDateTime.now();
        this.status = status;
    }
}
