package de.adorsys.ledgers.deposit.service;

import de.adorsys.ledgers.deposit.domain.BulkPayment;
import de.adorsys.ledgers.deposit.domain.DepositAccount;
import de.adorsys.ledgers.deposit.domain.PaymentResult;
import de.adorsys.ledgers.deposit.domain.SinglePayment;
import de.adorsys.ledgers.deposit.exception.PaymentProcessingException;

public interface DepositAccountService {

	DepositAccount createDepositAccount(DepositAccount depositAccount);

	PaymentResult executeSinglePaymentWithoutSca(SinglePayment payment) throws PaymentProcessingException;

	PaymentResult executeBulkPaymentWithoutSca(BulkPayment payment) throws PaymentProcessingException;
}
