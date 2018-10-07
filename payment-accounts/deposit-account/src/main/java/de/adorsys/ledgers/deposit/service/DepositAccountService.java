package de.adorsys.ledgers.deposit.service;

import de.adorsys.ledgers.deposit.domain.DepositAccount;
import de.adorsys.ledgers.postings.exception.NotFoundException;

public interface DepositAccountService {

	DepositAccount createDepositAccount(DepositAccount depositAccount, String ledgerName,
			String depositParentAccountNumber) throws NotFoundException;

}
