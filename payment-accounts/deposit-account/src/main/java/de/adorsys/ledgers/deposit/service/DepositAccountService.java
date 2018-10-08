package de.adorsys.ledgers.deposit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.adorsys.ledgers.deposit.domain.DepositAccount;
import de.adorsys.ledgers.deposit.domain.SinglePayment;
import de.adorsys.ledgers.postings.exception.NotFoundException;

public interface DepositAccountService {

	DepositAccount createDepositAccount(DepositAccount depositAccount);

	SinglePayment executeSinglePaymentWithoutSca(SinglePayment payment, String ledgerName) throws NotFoundException, JsonProcessingException;
}
