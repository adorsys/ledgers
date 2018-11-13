package de.adorsys.ledgers.deposit.db.repository;

import de.adorsys.ledgers.deposit.db.domain.PaymentTarget;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PaymentTargetRepository extends PagingAndSortingRepository<PaymentTarget, String> {
}
