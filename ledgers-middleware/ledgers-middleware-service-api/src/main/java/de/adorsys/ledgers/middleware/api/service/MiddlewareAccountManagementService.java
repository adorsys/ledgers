package de.adorsys.ledgers.middleware.api.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.api.domain.account.TransactionTO;
import de.adorsys.ledgers.middleware.api.exception.AccountNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.TransactionNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.UserNotFoundMiddlewareException;

public interface MiddlewareAccountManagementService {

	void createDepositAccount(AccountDetailsTO depositAccount) throws AccountNotFoundMiddlewareException;

	AccountDetailsTO getDepositAccountById(String Id, LocalDateTime time, boolean withBalance) throws AccountNotFoundMiddlewareException;

	AccountDetailsTO getDepositAccountByIBAN(String iban, LocalDateTime time, boolean withBalance)
			throws AccountNotFoundMiddlewareException;

    //============================ Account Details ==============================//

    AccountDetailsTO getAccountDetailsByAccountId(String accountId, LocalDateTime time) throws AccountNotFoundMiddlewareException;

    AccountDetailsTO getAccountDetailsByIban(String iban) throws AccountNotFoundMiddlewareException;

    AccountDetailsTO getAccountDetailsWithBalancesByIban(String iban, LocalDateTime refTime) throws AccountNotFoundMiddlewareException;

    List<AccountDetailsTO> getAllAccountDetailsByUserLogin(String userLogin) throws UserNotFoundMiddlewareException, AccountNotFoundMiddlewareException;

    TransactionTO getTransactionById(String accountId, String transactionId) throws AccountNotFoundMiddlewareException, TransactionNotFoundMiddlewareException;

    List<TransactionTO> getTransactionsByDates(String accountId, LocalDate dateFrom, LocalDate dateTo) throws AccountNotFoundMiddlewareException;
}
