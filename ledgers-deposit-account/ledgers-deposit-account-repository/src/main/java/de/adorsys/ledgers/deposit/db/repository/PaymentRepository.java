package de.adorsys.ledgers.deposit.db.repository;

import org.springframework.data.repository.PagingAndSortingRepository;

import de.adorsys.ledgers.deposit.db.domain.Payment;

public interface PaymentRepository extends PagingAndSortingRepository<Payment, String> {
}
