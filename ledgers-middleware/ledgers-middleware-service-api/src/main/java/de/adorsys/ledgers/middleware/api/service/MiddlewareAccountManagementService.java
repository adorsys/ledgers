package de.adorsys.ledgers.middleware.api.service;

import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.api.domain.account.FundsConfirmationRequestTO;
import de.adorsys.ledgers.middleware.api.domain.account.TransactionTO;
import de.adorsys.ledgers.middleware.api.domain.payment.AmountTO;
import de.adorsys.ledgers.middleware.api.domain.sca.SCAConsentResponseTO;
import de.adorsys.ledgers.middleware.api.domain.um.AccountAccessTO;
import de.adorsys.ledgers.middleware.api.domain.um.AisConsentTO;
import de.adorsys.ledgers.middleware.api.exception.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface MiddlewareAccountManagementService {

	/**
	 * Creates a new DepositAccount. This deposit account is then linked with the specified user.
	 *
	 * Call requires a bank staff access permission.
	 *
	 * @param UserID : user for who the account is being created
	 * @param depositAccount : the deposit account to be crated.
	 * @throws UserNotFoundMiddlewareException : if the associated user does not exist.
	 * @throws UserNotInBranchMiddlewareException : if the associated user is not in the same branch as staff member.
	 */
	void createDepositAccount(String UserID, AccountDetailsTO depositAccount)
            throws UserNotFoundMiddlewareException, UserNotInBranchMiddlewareException, AccountNotFoundMiddlewareException;

	/**
	 * Creates a new DepositAccount for the connected user.
	 * 
	 * @param accountNumberPrefix : the account number prefix : the account number prefix
	 * @param accountNumberSuffix : th eaccount number suffix
	 * @param accDetails : account to create.
	 * @throws AccountWithPrefixGoneMiddlewareException : another user owns this prefic
	 * @throws AccountWithSuffixExistsMiddlewareException : user has account with same prefix and this suffix
	 * @throws UserNotFoundMiddlewareException : no user
	 */
	void createDepositAccount(String accountNumberPrefix, String accountNumberSuffix, AccountDetailsTO accDetails)
			throws AccountWithPrefixGoneMiddlewareException, AccountWithSuffixExistsMiddlewareException, UserNotFoundMiddlewareException, AccountNotFoundMiddlewareException;

	/**
	 * Retrieve the list of account viewable by the connected user.
	 * @param userId : user identifier
	 * @return an empty list if user not linked with any deposit accounted.
	 */
	List<AccountDetailsTO> listDepositAccounts(String userId);

	/**
	 * TODO: return account or account details ???
	 * Retrieve the list of account registered for the branch.
	 * @param userId : user identifier
	 * @return list of accounts registered for the branch, or an empty list otherwise
	 */
	List<AccountDetailsTO> listDepositAccountsByBranch(String userId);
	
    /**
     * Retrieves AccountDetails with Balance on demand
     * @param Id DepositAccount identifier
     * @param time the reference time.
     * @param withBalance boolean specifying if Balances has to be added to AccountDetails
     * @return account details.
     * @throws AccountNotFoundMiddlewareException : target account not found.
	 * @throws InsufficientPermissionMiddlewareException : if the connected user is not linked ot the account.
     */
	AccountDetailsTO getDepositAccountById(String Id, LocalDateTime time, boolean withBalance) throws AccountNotFoundMiddlewareException, InsufficientPermissionMiddlewareException;

    /**
     * Retrieves AccountDetails with Balance on demand
     * @param iban DepositAccount iban
     * @param time the reference time.
     * @param withBalance boolean specifying if Balances has to be added to AccountDetails
     * @return account details.
     * @throws AccountNotFoundMiddlewareException : target account not found.
	 * @throws InsufficientPermissionMiddlewareException : if the connected user is not linked ot the account.
     */
	AccountDetailsTO getDepositAccountByIban(String iban, LocalDateTime time, boolean withBalance)
			throws AccountNotFoundMiddlewareException, InsufficientPermissionMiddlewareException;

    //============================ Account Details ==============================//
    /**
     * Retrieves a List of AccountDetails by user login (psuId)
     * @param userLogin the user login
     * @return list of account details.
     * @throws UserNotFoundMiddlewareException : if the associated user does not exist.
     * @throws AccountNotFoundMiddlewareException : target account not found.
	 * @throws InsufficientPermissionMiddlewareException : if the connected user is not linked ot the account.
     */
    List<AccountDetailsTO> getAllAccountDetailsByUserLogin(String userLogin) throws UserNotFoundMiddlewareException, AccountNotFoundMiddlewareException, InsufficientPermissionMiddlewareException;

    /**
     * Retrieves transaction by accountId and transactionId
     * @param accountId the account id
     * @param transactionId the transaction id
     * @return the corresponding transaction
     * @throws AccountNotFoundMiddlewareException : target account not found.
     * @throws TransactionNotFoundMiddlewareException : no transation with this id.
	 * @throws InsufficientPermissionMiddlewareException : if the connected user is not linked ot the account.
     */
    TransactionTO getTransactionById(String accountId, String transactionId) throws AccountNotFoundMiddlewareException, TransactionNotFoundMiddlewareException, InsufficientPermissionMiddlewareException;

    /**
     * Retrieves a List of transactions by accountId and dates (from/to) if dateTo is empty it is considered that requested date is today
     * @param accountId the account id
     * @param dateFrom from this time 
     * @param dateTo to this time
     * @return : List of transactions.
     * @throws AccountNotFoundMiddlewareException : target account not found.
	 * @throws InsufficientPermissionMiddlewareException : if the connected user is not linked of the account.
     */
    List<TransactionTO> getTransactionsByDates(String accountId, LocalDate dateFrom, LocalDate dateTo) throws AccountNotFoundMiddlewareException, InsufficientPermissionMiddlewareException;

    /**
     * Confirm the availability of funds on user account to perform the operation with specified amount
     * @param request : teh fund confirmation request.
     * @return : true if fund available else false.
     * @throws AccountNotFoundMiddlewareException : target account not found.
     */
    boolean confirmFundsAvailability(FundsConfirmationRequestTO request) throws AccountNotFoundMiddlewareException;

	String iban(String id);

	// ======================= CONSENT ======================//

	/**
	 * Start an account consent process.
	 *
	 * @param userId : the user id.
	 * @param consentId : the cosent id.
     * @param aisConsent : the consent details
	 * @return the corresponding access token describing the account access
	 *
	 * @throws InsufficientPermissionMiddlewareException : if the connected user is not linked ot the account.
	 */
	SCAConsentResponseTO startSCA(String userId, String consentId, AisConsentTO aisConsent)
			throws InsufficientPermissionMiddlewareException;

	SCAConsentResponseTO loadSCAForAisConsent(String userId, String consentId, String authorisationId) throws SCAOperationExpiredMiddlewareException, AisConsentNotFoundMiddlewareException;

	SCAConsentResponseTO selectSCAMethodForAisConsent(String userId, String consentId, String authorisationId, String scaMethodId) throws PaymentNotFoundMiddlewareException, SCAMethodNotSupportedMiddleException, UserScaDataNotFoundMiddlewareException, SCAOperationValidationMiddlewareException, SCAOperationNotFoundMiddlewareException, AisConsentNotFoundMiddlewareException;

	/**
	 * Authorizes a consent request. If the authentication is completed, the returned response will contain a valid bearer token.
	 *
	 * @param userId : the user id
	 * @param consentId : the cosent id
	 * @param authorisationId : the authorization id.
	 * @param authCode : SCAConsentResponseTO
	 * @return SCAConsentResponseTO : the consent response.
	 * @throws SCAOperationNotFoundMiddlewareException : operation no logger in db
	 * @throws SCAOperationValidationMiddlewareException : operation data not valid.
	 * @throws SCAOperationExpiredMiddlewareException : expired
	 * @throws SCAOperationUsedOrStolenMiddlewareException : user sca method not supported.
	 * @throws AisConsentNotFoundMiddlewareException : consent not found.
	 */
	SCAConsentResponseTO authorizeConsent(String userId, String consentId, String authorisationId, String authCode)
			throws SCAOperationNotFoundMiddlewareException, SCAOperationValidationMiddlewareException,
			SCAOperationExpiredMiddlewareException, SCAOperationUsedOrStolenMiddlewareException,
			AisConsentNotFoundMiddlewareException;

	/**
	 * Provide a third party provider with necessary permission to read accounts and
	 * transaction informations for the specified account.
	 *
     * @param aisConsent : the consent details
	 * @return the corresponding access token describing the account access
	 *
     * @throws AccountNotFoundMiddlewareException : target account not found.
	 * @throws InsufficientPermissionMiddlewareException : if the connected user is not linked ot the account.
	 */
	SCAConsentResponseTO grantAisConsent(AisConsentTO aisConsent)
			throws AccountNotFoundMiddlewareException, InsufficientPermissionMiddlewareException;

	/**
	 * Deposits given amount in cash into specified account.
	 * On the bank's books, the bank debits its cash account for the given amount in cash,
	 * and credits a "deposits" liability account for an equal amount.
	 * @param accountId id of the account deposited into
	 * @param amount amount of cash deposited
	 * @throws AccountNotFoundMiddlewareException target account not found
	 */
	void depositCash(String accountId, AmountTO amount) throws AccountNotFoundMiddlewareException;

	/**
	 * Retrieves a List of AccountAccessTO by userId
	 * @param userId id of the user
	 */
	List<AccountAccessTO> getAccountAccesses(String userId);
}
