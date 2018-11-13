package de.adorsys.ledgers.deposit.api.service.impl;

import de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO;
import de.adorsys.ledgers.deposit.api.exception.PaymentNotFoundException;
import de.adorsys.ledgers.deposit.api.exception.PaymentProcessingException;
import de.adorsys.ledgers.deposit.api.service.DepositAccountTransactionService;
import de.adorsys.ledgers.deposit.api.service.PaymentSchedulerService;
import de.adorsys.ledgers.deposit.db.domain.Payment;
import de.adorsys.ledgers.deposit.db.domain.PaymentType;
import de.adorsys.ledgers.deposit.db.domain.ScheduledPaymentOrder;
import de.adorsys.ledgers.deposit.db.domain.TransactionStatus;
import de.adorsys.ledgers.deposit.db.repository.PaymentRepository;
import de.adorsys.ledgers.deposit.db.repository.ScheduledPaymentOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@Service
public class PaymentSchedulerServiceImpl implements PaymentSchedulerService {
    private final ScheduledPaymentOrderRepository scheduledPaymentOrderRepository;
    private final PaymentRepository paymentRepository;
    private final DepositAccountTransactionService txService;

    @Autowired
    public PaymentSchedulerServiceImpl(ScheduledPaymentOrderRepository scheduledPaymentOrderRepository, PaymentRepository paymentRepository, DepositAccountTransactionService txService) {
        this.scheduledPaymentOrderRepository = scheduledPaymentOrderRepository;
        this.paymentRepository = paymentRepository;
        this.txService = txService;
    }

    /**
     * Proceeds with the first scheduling of a payment.
     */
    @Override
    public TransactionStatusBO schedulePaymentExecution(String paymentOrderId) throws PaymentNotFoundException {
        // Verify that there is no 
        // Check if any scheduled payment, ant throw an exception.
        Optional<ScheduledPaymentOrder> found = scheduledPaymentOrderRepository.findById(paymentOrderId);
        if (found.isPresent()) {
            throw new PaymentProcessingException("Can not reschedule a payment.");
        }

        // Load the payment.
        Payment payment = paymentRepository.findById(paymentOrderId)
                                  .orElseThrow(() -> new PaymentNotFoundException(paymentOrderId));

        // How do we proceed with periodic Payment
        if (PaymentType.PERIODIC.equals(payment.getPaymentType())) {
            return schedulePeriodicPmt(payment);
        } else {
            return scheduleOneTimePmt(payment);
        }
    }

    private TransactionStatusBO scheduleOneTimePmt(Payment payment) throws PaymentNotFoundException {
        LocalDate requestedExecutionDate = payment.getRequestedExecutionDate() == null
                                                   ? LocalDate.now()
                                                   : payment.getRequestedExecutionDate();
        LocalTime requestedExecutionTime = payment.getRequestedExecutionTime() == null
                                                   ? LocalTime.now()
                                                   : payment.getRequestedExecutionTime();

        LocalDateTime postingTime = LocalDateTime.of(requestedExecutionDate, requestedExecutionTime);
        return txService.bookPayment(payment.getPaymentId(), postingTime);
    }

    private TransactionStatusBO schedulePeriodicPmt(Payment payment) {
        LocalDateTime now = LocalDateTime.now();
        // Set startDate to now if none.
        LocalDate startDate = payment.getStartDate() == null
                                      ? now.toLocalDate()
                                      : payment.getStartDate();

        ScheduledPaymentOrder po = newScheduledPaymentOrderObj(payment, now, startDate);
        scheduledPaymentOrderRepository.save(po);
        payment.setTransactionStatus(TransactionStatus.ACWP);
        Payment saved = paymentRepository.save(payment);
        return TransactionStatusBO.valueOf(saved.getTransactionStatus().name());
    }

    private ScheduledPaymentOrder newScheduledPaymentOrderObj(Payment pymt, LocalDateTime now,
                                                              LocalDate startDate) {
        ScheduledPaymentOrder po = new ScheduledPaymentOrder();
        po.setPaymentOrderId(pymt.getPaymentId());
        po.setNextExecTime(now);
        po.setNextPostingTime(LocalDateTime.of(startDate, LocalTime.MIN));
        po.setExecStatusTime(now);
        return po;
    }
}
