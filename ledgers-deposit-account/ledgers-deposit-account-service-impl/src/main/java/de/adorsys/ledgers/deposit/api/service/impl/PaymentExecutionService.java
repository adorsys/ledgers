package de.adorsys.ledgers.deposit.api.service.impl;

import de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO;
import de.adorsys.ledgers.deposit.api.service.DepositAccountTransactionService;
import de.adorsys.ledgers.deposit.db.domain.Payment;
import de.adorsys.ledgers.deposit.db.domain.PaymentType;
import de.adorsys.ledgers.deposit.db.domain.TransactionStatus;
import de.adorsys.ledgers.deposit.db.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import net.objectlab.kit.datecalc.common.DateCalculator;
import net.objectlab.kit.datecalc.common.DefaultHolidayCalendar;
import net.objectlab.kit.datecalc.common.HolidayCalendar;
import net.objectlab.kit.datecalc.jdk8.LocalDateKitCalculatorsFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import pro.javatar.commons.reader.YamlReader;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentExecutionService implements InitializingBean {
    private static final String CALENDAR_NAME = "LEDGERS";
    private static final String PRECEDING = "preceeding";
    private final PaymentRepository paymentRepository;
    private final DepositAccountTransactionService txService;

    @Override
    public void afterPropertiesSet() {
        HolidayCalendar<LocalDate> calendar = new DefaultHolidayCalendar<>(new HashSet<>(readHolidays()));
        LocalDateKitCalculatorsFactory.getDefaultInstance().registerHolidays("DE", calendar);
    }

    public TransactionStatusBO executePayment(Payment payment, String userName) {
        LocalDateTime executionTime = LocalDateTime.now();
        txService.bookPayment(payment, executionTime, userName);
        payment.setExecutedDate(executionTime);

        if (payment.getPaymentType() == PaymentType.PERIODIC) {
            return schedulePayment(payment);
        } else {
            payment.setTransactionStatus(TransactionStatus.ACSC);
            payment.setNextScheduledExecution(null);
            Payment savedPayment = paymentRepository.save(payment);
            return TransactionStatusBO.valueOf(savedPayment.getTransactionStatus().name());
        }
    }

    public TransactionStatusBO schedulePayment(Payment payment) {
        LocalDate executionDate = calculateExecutionDate(payment);
        TransactionStatus status = executionDate == null
                                           ? TransactionStatus.ACSC
                                           : TransactionStatus.ACSP;
        payment.setTransactionStatus(status);
        LocalDateTime executionDateTime = null;
        if (executionDate != null) {
            LocalTime executionTime = payment.getRequestedExecutionTime() == null
                                              ? LocalTime.MIN
                                              : payment.getRequestedExecutionTime();
            executionDateTime = LocalDateTime.of(executionDate, executionTime);
        }
        payment.setNextScheduledExecution(executionDateTime);
        Payment savedPayment = paymentRepository.save(payment);
        return TransactionStatusBO.valueOf(savedPayment.getTransactionStatus().name());
    }

    private LocalDate calculateExecutionDate(Payment payment) {
        LocalDate date = payment.getPaymentType() == PaymentType.PERIODIC
                                 ? calculateForPeriodicPmt(payment)
                                 : calculateForRegularPmt(payment);

        return payment.isLastExecuted(date)
                       ? null
                       : calculateBusinessDate(payment.getExecutionRule(), date);
    }

    private LocalDate calculateForRegularPmt(Payment payment) {
        return payment.getRequestedExecutionDate() != null && !payment.getRequestedExecutionDate().isBefore(LocalDate.now())
                       ? payment.getRequestedExecutionDate()
                       : LocalDate.now();
    }

    private LocalDate calculateForPeriodicPmt(Payment payment) {
        return payment.getExecutedDate() == null
                       ? payment.getStartDate()
                       : ExecutionTimeHolder.getExecutionDate(payment);
    }

    private LocalDate calculateBusinessDate(String executionRule, LocalDate date) {
        DateCalculator<LocalDate> calc = PRECEDING.equals(executionRule)
                                                 ? LocalDateKitCalculatorsFactory.backwardCalculator(CALENDAR_NAME)
                                                 : LocalDateKitCalculatorsFactory.forwardCalculator(CALENDAR_NAME);
        calc.setStartDate(date);
        return calc.getCurrentBusinessDate();
    }

    private static List<LocalDate> readHolidays() {
        try {
            return YamlReader.getInstance().getListFromFile("holidays.yml", LocalDate.class);
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
