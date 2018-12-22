package de.adorsys.ledgers.deposit.api.service.impl;

import de.adorsys.ledgers.deposit.db.domain.Payment;
import de.adorsys.ledgers.deposit.db.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentExecutionScheduler {
    private static final String SCHEDULER = "Scheduler";
    private static final Logger logger = LoggerFactory.getLogger(PaymentExecutionScheduler.class);
    private final PaymentRepository paymentRepository;
    private final PaymentExecutionService executionService;

    public PaymentExecutionScheduler(PaymentRepository repository, PaymentExecutionService executionService) {
        this.paymentRepository = repository;
        this.executionService = executionService;
    }

    @Scheduled(initialDelayString = "${paymentScheduler.initialDelay}", fixedDelayString = "${paymentScheduler.delay}")
    public void scheduler() {
        logger.info("Scheduler started at {}", LocalDateTime.now());
        List<Payment> payments = paymentRepository.getAllDuePayments();
        payments.forEach(p -> executionService.executePayment(p, SCHEDULER));
        logger.info("Scheduler executed : {} payments", payments.size());
    }
}
