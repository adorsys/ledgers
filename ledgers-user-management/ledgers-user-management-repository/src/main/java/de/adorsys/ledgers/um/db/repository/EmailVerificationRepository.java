/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.um.db.repository;

import de.adorsys.ledgers.um.db.domain.EmailVerificationEntity;
import de.adorsys.ledgers.um.db.domain.EmailVerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerificationEntity, Long> {

    Optional<EmailVerificationEntity> findByScaUserDataIdAndStatusNot(String scaId, EmailVerificationStatus status);

    Optional<EmailVerificationEntity> findByTokenAndStatus(String token, EmailVerificationStatus status);

    Optional<EmailVerificationEntity> findByToken(String token);

    @Modifying
    @Query("DELETE FROM EmailVerificationEntity WHERE scaUserData.id =?1")
    void deleteByScaId(String scaId);
}
