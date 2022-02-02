package de.adorsys.ledgers.deposit.db.repository;

import de.adorsys.ledgers.deposit.db.domain.Payment;
import de.adorsys.ledgers.deposit.db.domain.PaymentType;
import de.adorsys.ledgers.deposit.db.domain.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PaymentRepository extends PagingAndSortingRepository<Payment, String> {
    Optional<Payment> findByPaymentIdAndTransactionStatus(String paymentId, TransactionStatus status);

    @Transactional
    @Query(value = "select p from Payment as p where p.transactionStatus = 'ACSP' and p.nextScheduledExecution <= current_timestamp")
    List<Payment> getAllDuePayments();

    List<Payment> findAllByAccountIdInAndPaymentTypeAndTransactionStatus(Set<String> accountId, PaymentType type, TransactionStatus status);

    Page<Payment> findAllByAccountIdInAndPaymentTypeAndTransactionStatus(Set<String> accountId, PaymentType type, TransactionStatus status, Pageable pageable);

    Page<Payment> findAllByAccountIdInAndPaymentTypeInAndTransactionStatusInOrderByUpdatedDesc(Set<String> accountId, Set<PaymentType> type, Set<TransactionStatus> status, Pageable pageable);

}
