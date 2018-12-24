package de.adorsys.ledgers.deposit.db.repository;

import de.adorsys.ledgers.deposit.db.domain.Payment;
import de.adorsys.ledgers.deposit.db.domain.TransactionStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends PagingAndSortingRepository<Payment, String> {
    Optional<Payment> findByPaymentIdAndTransactionStatus(String paymentId, TransactionStatus status);

    @Query(value = "select p from Payment as p where p.transactionStatus = 'ACSP' and p.nextScheduledExecution <= current_timestamp")
    List<Payment> getAllDuePayments();
}
