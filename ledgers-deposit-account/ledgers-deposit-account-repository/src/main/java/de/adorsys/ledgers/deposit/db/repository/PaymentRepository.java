package de.adorsys.ledgers.deposit.db.repository;

import de.adorsys.ledgers.deposit.db.domain.Payment;
import de.adorsys.ledgers.deposit.db.domain.PaymentType;
import de.adorsys.ledgers.deposit.db.domain.TransactionStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends PagingAndSortingRepository<Payment, String> {
    Optional<Payment> findByPaymentIdAndTransactionStatus(String paymentId, TransactionStatus status);

    @Query(value = "select p from Payment as p where p.transactionStatus = 'ACSP' and p.nextScheduledExecution <= current_timestamp")
    List<Payment> getAllDuePayments();

    @Query(value = "select p from Payment as p where p.debtorAccount.iban=?1 and p.debtorAccount.currency=?2 and p.paymentType=?3 and p.transactionStatus=?4")
    List<Payment> findAllByDebtorAccount(String iban, String currency, PaymentType type, TransactionStatus status);
}
