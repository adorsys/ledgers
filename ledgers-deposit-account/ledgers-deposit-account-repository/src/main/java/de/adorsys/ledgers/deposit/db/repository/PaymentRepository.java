package de.adorsys.ledgers.deposit.db.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import de.adorsys.ledgers.deposit.db.domain.Payment;
import de.adorsys.ledgers.deposit.db.domain.TransactionStatus;

public interface PaymentRepository extends PagingAndSortingRepository<Payment, String> {
    Optional<Payment> findByPaymentId(String paymentId);
    Optional<Payment> findByPaymentIdAndTransactionStatus(String paymentId, TransactionStatus status);

    @Query(value = "select p from Payment as p where p.transactionStatus = 'ACSP' and p.nextScheduledExecution <= current_timestamp")
    List<Payment> getAllDuePayments();
}
