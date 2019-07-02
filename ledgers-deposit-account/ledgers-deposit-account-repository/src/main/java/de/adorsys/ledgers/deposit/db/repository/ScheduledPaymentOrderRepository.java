package de.adorsys.ledgers.deposit.db.repository;

import de.adorsys.ledgers.deposit.db.domain.ScheduledPaymentOrder;
import org.springframework.data.repository.PagingAndSortingRepository;

//TODO REMOVE unused REPO
public interface ScheduledPaymentOrderRepository extends PagingAndSortingRepository<ScheduledPaymentOrder, String> {
}
