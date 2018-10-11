package de.adorsys.ledgers.deposit.service;

import de.adorsys.ledgers.deposit.domain.BulkPayment;
import de.adorsys.ledgers.deposit.domain.DepositAccount;
import de.adorsys.ledgers.deposit.domain.SinglePayment;
import de.adorsys.ledgers.deposit.exception.PaymentProcessingException;

public interface DepositAccountService {

	DepositAccount createDepositAccount(DepositAccount depositAccount);

//	todo: ask @fpo are we really nead @ledgerName parameter for payment?
	SinglePayment executeSinglePayment(SinglePayment payment, String ledgerName) throws PaymentProcessingException;

	SinglePayment executeBulkPayment(BulkPayment payment, String ledgerName) throws PaymentProcessingException;
}
