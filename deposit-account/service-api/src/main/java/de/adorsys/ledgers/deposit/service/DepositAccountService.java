package de.adorsys.ledgers.deposit.service;

import de.adorsys.ledgers.deposit.domain.BulkPaymentBO;
import de.adorsys.ledgers.deposit.domain.DepositAccountBO;
import de.adorsys.ledgers.deposit.domain.PaymentResultBO;
import de.adorsys.ledgers.deposit.domain.SinglePaymentBO;
import de.adorsys.ledgers.deposit.exception.PaymentProcessingException;

public interface DepositAccountService {

	DepositAccountBO createDepositAccount(DepositAccountBO depositAccount) throws PaymentProcessingException;

	PaymentResultBO executeSinglePaymentWithoutSca(SinglePaymentBO payment) throws PaymentProcessingException;

	PaymentResultBO executeBulkPaymentWithoutSca(BulkPaymentBO payment) throws PaymentProcessingException;
}
