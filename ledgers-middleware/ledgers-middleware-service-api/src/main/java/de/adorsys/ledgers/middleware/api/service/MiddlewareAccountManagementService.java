package de.adorsys.ledgers.middleware.api.service;

import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.api.domain.account.FundsConfirmationRequestTO;
import de.adorsys.ledgers.middleware.api.domain.account.TransactionTO;
import de.adorsys.ledgers.middleware.api.exception.AccountNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.TransactionNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.UserNotFoundMiddlewareException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface MiddlewareAccountManagementService {

    /**
     * Creates a new DepositAccount
     * @param depositAccount
     * @throws AccountNotFoundMiddlewareException
     */
	void createDepositAccount(AccountDetailsTO depositAccount) throws AccountNotFoundMiddlewareException;

    /**
     * Retrieves AccountDetails with Balance on demand
     * @param Id DepositAccount identifier
     * @param time
     * @param withBalance boolean specifying if Balances has to be added to AccountDetails
     * @return
     * @throws AccountNotFoundMiddlewareException
     */
	AccountDetailsTO getDepositAccountById(String Id, LocalDateTime time, boolean withBalance) throws AccountNotFoundMiddlewareException;

    /**
     * Retrieves AccountDetails with Balance on demand
     * @param iban
     * @param time
     * @param withBalance
     * @return
     * @throws AccountNotFoundMiddlewareException
     */
	AccountDetailsTO getDepositAccountByIban(String iban, LocalDateTime time, boolean withBalance)
			throws AccountNotFoundMiddlewareException;

    //============================ Account Details ==============================//
    /**
     * Retrieves a List of AccountDetails by user login (psuId)
     * @param userLogin
     * @return
     * @throws UserNotFoundMiddlewareException
     * @throws AccountNotFoundMiddlewareException
     */
    List<AccountDetailsTO> getAllAccountDetailsByUserLogin(String userLogin) throws UserNotFoundMiddlewareException, AccountNotFoundMiddlewareException;

    /**
     * Retrieves transaction by accountId and transactionId
     * @param accountId
     * @param transactionId
     * @return
     * @throws AccountNotFoundMiddlewareException
     * @throws TransactionNotFoundMiddlewareException
     */
    TransactionTO getTransactionById(String accountId, String transactionId) throws AccountNotFoundMiddlewareException, TransactionNotFoundMiddlewareException;

    /**
     * Retrieves a List of transactions by accountId and dates (from/to) if dateTo is empty it is considered that requested date is today
     * @param accountId
     * @param dateFrom
     * @param dateTo
     * @return
     * @throws AccountNotFoundMiddlewareException
     */
    List<TransactionTO> getTransactionsByDates(String accountId, LocalDate dateFrom, LocalDate dateTo) throws AccountNotFoundMiddlewareException;

    /**
     * Confirm the availability of funds on user account to perform the operation with specified amount
     * @param request
     * @return
     * @throws AccountNotFoundMiddlewareException
     */
    boolean confirmFundsAvailability(FundsConfirmationRequestTO request) throws AccountNotFoundMiddlewareException;
}
