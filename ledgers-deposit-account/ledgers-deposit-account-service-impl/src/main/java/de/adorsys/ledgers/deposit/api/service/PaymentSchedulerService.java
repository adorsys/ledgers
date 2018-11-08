package de.adorsys.ledgers.deposit.api.service;

import de.adorsys.ledgers.deposit.api.domain.TransactionStatusBO;
import de.adorsys.ledgers.deposit.api.exception.PaymentNotFoundException;

public interface PaymentSchedulerService {
	
	TransactionStatusBO schedulePaymentExecution(String paymentOrderId) throws PaymentNotFoundException;
}
