package de.adorsys.ledgers.deposit.api.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

@Service
public class PaymentSchedulerServiceImpl implements PaymentSchedulerService {
	
	@Autowired
	private ScheduledPaymentOrderRepository scheduledPaymentOrderRepository;
	
	@Autowired
	private PaymentRepository paymentRepository;
	
	@Autowired
	private DepositAccountTransactionService txService;

	/**
	 * Proceeds with the first scheduling of a payment.
	 */
	@Override
	public TransactionStatusBO schedulePaymentExecution(String paymentOrderId) throws PaymentNotFoundException {        
        // Verify that there is no 
        // Check if any scheduled payment, ant throw an exception.
        Optional<ScheduledPaymentOrder> found = scheduledPaymentOrderRepository.findById(paymentOrderId);
        if(found.isPresent()) {
        	throw new PaymentProcessingException("Can not reschedule a payment.");
        }
        
		// Load the payment.
        Payment pymt = paymentRepository.findById(paymentOrderId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentOrderId));
        
        // How do we proceed with periodic Payment
        if(PaymentType.PERIODIC.equals(pymt.getPaymentType())){
        	return schedulePeriodicPymt(pymt);
        } else {
            return scheduleOneTimePymt(pymt);
        }
	}

	private TransactionStatusBO scheduleOneTimePymt(Payment pymt) throws PaymentNotFoundException {
		LocalDate requestedExecutionDate = pymt.getRequestedExecutionDate()==null?LocalDate.now():pymt.getRequestedExecutionDate();
		LocalTime requestedExecutionTime = pymt.getRequestedExecutionTime()==null?LocalTime.now():pymt.getRequestedExecutionTime();
		
		LocalDateTime postingTime = LocalDateTime.of(requestedExecutionDate, requestedExecutionTime);
		return txService.bookPayment(pymt.getPaymentId(), postingTime);
	}

	private TransactionStatusBO schedulePeriodicPymt(Payment pymt) {
		LocalDateTime now = LocalDateTime.now();
		// Set startDate to now if none.
		LocalDate startDate = pymt.getStartDate()==null
				? now.toLocalDate()
				: pymt.getStartDate();

		ScheduledPaymentOrder po = newScheduledPaymentOrderObj(pymt, now, startDate);
		scheduledPaymentOrderRepository.save(po);
		pymt.setTransactionStatus(TransactionStatus.ACWP);
		Payment saved = paymentRepository.save(pymt);
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
