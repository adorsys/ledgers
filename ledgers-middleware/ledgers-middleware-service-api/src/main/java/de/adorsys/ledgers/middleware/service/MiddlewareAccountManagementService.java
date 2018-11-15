package de.adorsys.ledgers.middleware.service;

import de.adorsys.ledgers.middleware.service.domain.account.DepositAccountTO;
import de.adorsys.ledgers.middleware.service.exception.AccountNotFoundMiddlewareException;

public interface MiddlewareAccountManagementService {

	void createDepositAccount(DepositAccountTO depositAccount) throws AccountNotFoundMiddlewareException;

	DepositAccountTO getDepositAccountByIBAN(String iban) throws AccountNotFoundMiddlewareException;

}
