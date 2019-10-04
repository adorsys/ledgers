package de.adorsys.ledgers.deposit.api.service.impl;

import de.adorsys.ledgers.deposit.api.domain.*;
import de.adorsys.ledgers.deposit.api.exception.DepositModuleException;
import de.adorsys.ledgers.deposit.api.service.DepositAccountService;
import de.adorsys.ledgers.deposit.api.service.DepositAccountTransactionService;
import de.adorsys.ledgers.deposit.api.service.mappers.PaymentMapper;
import de.adorsys.ledgers.deposit.db.domain.Payment;
import de.adorsys.ledgers.deposit.db.domain.PaymentType;
import de.adorsys.ledgers.deposit.db.domain.TransactionStatus;
import de.adorsys.ledgers.deposit.db.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.objectlab.kit.datecalc.common.DateCalculator;
import net.objectlab.kit.datecalc.common.DefaultHolidayCalendar;
import net.objectlab.kit.datecalc.common.HolidayCalendar;
import net.objectlab.kit.datecalc.jdk8.LocalDateKitCalculatorsFactory;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import pro.javatar.commons.reader.YamlReader;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Currency;
import java.util.HashSet;
import java.util.List;

import static de.adorsys.ledgers.deposit.api.exception.DepositErrorCode.PAYMENT_PROCESSING_FAILURE;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentExecutionService implements InitializingBean {
    private static final String CALENDAR_NAME = "LEDGERS";
    private static final String PRECEDING = "preceding";
    private final PaymentRepository paymentRepository;
    private final DepositAccountTransactionService txService;
    private final DepositAccountService accountService;
    private final PaymentMapper paymentMapper = Mappers.getMapper(PaymentMapper.class);

    @Override
    public void afterPropertiesSet() {
        HolidayCalendar<LocalDate> calendar = new DefaultHolidayCalendar<>(new HashSet<>(readHolidays()));
        LocalDateKitCalculatorsFactory.getDefaultInstance().registerHolidays("DE", calendar);
    }

    public TransactionStatusBO executePayment(Payment payment, String userName) {
        PaymentBO paymentBO = paymentMapper.toPaymentBO(payment);
        AmountBO amountToVerify = calculateTotalPaymentAmount(paymentBO);
        boolean confirmationOfFunds = accountService.confirmationOfFunds(new FundsConfirmationRequestBO(null, paymentBO.getDebtorAccount(), amountToVerify, null, null));

        if (!confirmationOfFunds) {
            updatePaymentStatus(payment, TransactionStatus.RJCT);
            log.info("Scheduler couldn't execute payment : {}. Insufficient funds to complete the operation", payment.getTransactionStatus());
            return TransactionStatusBO.RJCT;
        }
        LocalDateTime executionTime = LocalDateTime.now();
        txService.bookPayment(payment, executionTime, userName);
        payment.setExecutedDate(executionTime);

        return payment.getPaymentType() == PaymentType.PERIODIC
                       ? schedulePayment(payment)
                       : updatePaymentStatus(payment, TransactionStatus.ACSC);
    }

    private TransactionStatusBO updatePaymentStatus(Payment payment, TransactionStatus status) {
        payment.setTransactionStatus(status);
        payment.setNextScheduledExecution(null);
        paymentRepository.save(payment);
        return TransactionStatusBO.valueOf(status.name());
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

    public AmountBO calculateTotalPaymentAmount(PaymentBO payment) {
        return payment.getTargets().stream()
                       .map(PaymentTargetBO::getInstructedAmount)
                       .reduce((left, right) -> new AmountBO(Currency.getInstance("EUR"), left.getAmount().add(right.getAmount())))
                       .orElseThrow(() -> DepositModuleException.builder()
                                                  .errorCode(PAYMENT_PROCESSING_FAILURE)
                                                  .devMsg(String.format("Could not calculate total amount for payment: %s.", payment.getPaymentId()))
                                                  .build());
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
                       ? nextDayOfExecution(payment)
                       : ExecutionTimeHolder.getExecutionDate(payment);
    }

    private static LocalDate nextDayOfExecution(Payment payment){
        if (payment.getStartDate().isAfter(payment.getStartDate().withDayOfMonth(payment.getDayOfExecution()))) {
            return payment.getStartDate();
        }
        return payment.getStartDate().withDayOfMonth(payment.getDayOfExecution());
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
            throw DepositModuleException.builder().errorCode(PAYMENT_PROCESSING_FAILURE).devMsg(e.getMessage()).build();
        }
    }
}
