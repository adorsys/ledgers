package de.adorsys.ledgers.middleware.service;

import java.time.LocalDateTime;

import de.adorsys.ledgers.middleware.service.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.service.exception.AccountNotFoundMiddlewareException;

public interface MiddlewareAccountManagementService {

	void createDepositAccount(AccountDetailsTO depositAccount) throws AccountNotFoundMiddlewareException;

	AccountDetailsTO getDepositAccountById(String Id, LocalDateTime time, boolean withBalance) throws AccountNotFoundMiddlewareException;

	AccountDetailsTO getDepositAccountByIBAN(String iban, LocalDateTime time, boolean withBalance)
			throws AccountNotFoundMiddlewareException;

}
