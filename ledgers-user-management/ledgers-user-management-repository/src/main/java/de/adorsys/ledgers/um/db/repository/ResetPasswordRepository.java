package de.adorsys.ledgers.um.db.repository;

import de.adorsys.ledgers.um.db.domain.ResetPasswordEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ResetPasswordRepository extends CrudRepository<ResetPasswordEntity, Long> {
    Optional<ResetPasswordEntity> findByCode(String code);
    Optional<ResetPasswordEntity> findByUserId(String userId);
}
