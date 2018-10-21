package de.adorsys.ledgers.deposit.api.service;

import java.util.List;

import de.adorsys.ledgers.deposit.api.domain.BulkPaymentBO;
import de.adorsys.ledgers.deposit.api.domain.DepositAccountBO;
import de.adorsys.ledgers.deposit.api.domain.PaymentResultBO;
import de.adorsys.ledgers.deposit.api.domain.SinglePaymentBO;
import de.adorsys.ledgers.deposit.api.exception.PaymentProcessingException;

public interface DepositAccountService {

	DepositAccountBO createDepositAccount(DepositAccountBO depositAccount) throws PaymentProcessingException;

	PaymentResultBO executeSinglePaymentWithoutSca(SinglePaymentBO payment) throws PaymentProcessingException;

	PaymentResultBO executeBulkPaymentWithoutSca(BulkPaymentBO payment) throws PaymentProcessingException;

	PaymentResultBO executeSinglePaymentsWithoutSca(List<SinglePaymentBO> paymentBOList)
			throws PaymentProcessingException;
}
