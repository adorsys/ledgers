package de.adorsys.ledgers.deposit.api.service.impl;

import de.adorsys.ledgers.deposit.db.domain.Payment;
import de.adorsys.ledgers.deposit.db.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentExecutionScheduler {
    private static final String SCHEDULER = "Scheduler";
    private final PaymentRepository paymentRepository;
    private final PaymentExecutionService executionService;


    @Scheduled(initialDelayString = "${paymentScheduler.initialDelay}", fixedDelayString = "${paymentScheduler.delay}")
    public void scheduler() {
        log.info("Scheduler started at {}", LocalDateTime.now());
        List<Payment> payments = paymentRepository.getAllDuePayments();
        payments.forEach(p -> executionService.executePayment(p, SCHEDULER));
        log.info("Scheduler executed : {} payments", payments.size());
    }
}
