package de.adorsys.ledgers.deposit.db.repository;

import org.springframework.data.repository.PagingAndSortingRepository;

import de.adorsys.ledgers.deposit.db.domain.ScheduledPaymentOrder;

public interface ScheduledPaymentOrderRepository extends PagingAndSortingRepository<ScheduledPaymentOrder, String> {
}
