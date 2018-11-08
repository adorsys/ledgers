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
        Payment storedPayment = paymentRepository.findById(paymentOrderId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentOrderId));
        
        LocalDateTime now = LocalDateTime.now();
        // How do we proceed with periodic Payment
        if(PaymentType.PERIODIC.equals(storedPayment.getPaymentType())){
        	// Set startDate to now if none.
    		LocalDate startDate = storedPayment.getStartDate();
    		if(startDate==null) {
    			startDate = now.toLocalDate();
    		}
            ScheduledPaymentOrder po = new ScheduledPaymentOrder();
            po.setPaymentOrderId(paymentOrderId);
            po.setNextExecTime(now);
            po.setNextPostingTime(LocalDateTime.of(startDate, LocalTime.MIN));
            po.setPaymentOrderId(paymentOrderId);
            po.setExecStatusTime(LocalDateTime.now());
            scheduledPaymentOrderRepository.save(po);
            storedPayment.setTransactionStatus(TransactionStatus.ACWP);
            storedPayment = paymentRepository.save(storedPayment);
            return TransactionStatusBO.valueOf(storedPayment.getTransactionStatus().name());
        } else {
            LocalDate requestedExecutionDate = storedPayment.getRequestedExecutionDate()==null?LocalDate.now():storedPayment.getRequestedExecutionDate();
            LocalTime requestedExecutionTime = storedPayment.getRequestedExecutionTime()==null?LocalTime.now():storedPayment.getRequestedExecutionTime();
            
            LocalDateTime postingTime = LocalDateTime.of(requestedExecutionDate, requestedExecutionTime);
            return txService.executePayment(paymentOrderId, postingTime);
        }
        
		
	}
}
