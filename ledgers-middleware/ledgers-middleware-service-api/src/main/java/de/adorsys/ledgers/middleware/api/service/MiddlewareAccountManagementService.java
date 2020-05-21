package de.adorsys.ledgers.middleware.api.service;

import de.adorsys.ledgers.middleware.api.domain.account.*;
import de.adorsys.ledgers.middleware.api.domain.payment.AmountTO;
import de.adorsys.ledgers.middleware.api.domain.sca.SCAConsentResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.um.AccountAccessTO;
import de.adorsys.ledgers.middleware.api.domain.um.AisConsentTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.util.domain.CustomPageImpl;
import de.adorsys.ledgers.util.domain.CustomPageableImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface MiddlewareAccountManagementService {

    List<AccountDetailsTO> getAccountsByIbanAndCurrency(String iban, String currency);

    /**
     * Creates a new DepositAccount. This deposit account is then linked with the specified user.
     * <p>
     * Call requires a bank staff access permission.
     *
     * @param userId:        the identifier of the user for whom the account is created
     * @param scaInfoTO      : SCA information
     * @param depositAccount : the deposit account to be crated.
     */
    void createDepositAccount(String userId, ScaInfoTO scaInfoTO, AccountDetailsTO depositAccount);

    /**
     * Creates a new DepositAccount for the connected user.
     *
     * @param scaInfoTO           : SCA information
     * @param accountNumberPrefix : the account number prefix : the account number prefix
     * @param accountNumberSuffix : th eaccount number suffix
     * @param accDetails          : account to create.
     */
    void createDepositAccount(ScaInfoTO scaInfoTO, String accountNumberPrefix, String accountNumberSuffix, AccountDetailsTO accDetails);

    /**
     * Retrieve the list of account viewable by the connected user.
     *
     * @param userId : user identifier
     * @return an empty list if user not linked with any deposit accounted.
     */
    List<AccountDetailsTO> listDepositAccounts(String userId);

    /**
     * TODO: return account or account details ???
     * Retrieve the list of account registered for the branch.
     *
     * @param userId : user identifier
     * @return list of accounts registered for the branch, or an empty list otherwise
     */
    List<AccountDetailsTO> listDepositAccountsByBranch(String userId);

    CustomPageImpl<AccountDetailsTO> listDepositAccountsByBranchPaged(String userId, String queryParam, CustomPageableImpl pageable);

    CustomPageImpl<AccountDetailsExtendedTO> getAccountsByBranchAndMultipleParams(String countryCode, String branchId, String branchLogin, String iban, Boolean blocked, CustomPageableImpl pageable);

    /**
     * Retrieves AccountDetails with Balance on demand
     *
     * @param id          DepositAccount identifier
     * @param time        the reference time.
     * @param withBalance boolean specifying if Balances has to be added to AccountDetails
     * @return account details.
     */
    AccountDetailsTO getDepositAccountById(String id, LocalDateTime time, boolean withBalance);

    /**
     * Retrieves AccountDetails with Balance on demand
     *
     * @param iban        DepositAccount iban
     * @param time        the reference time.
     * @param withBalance boolean specifying if Balances has to be added to AccountDetails
     * @return account details.
     * @deprecated shall be removed in v2.5
     */
    AccountDetailsTO getDepositAccountByIban(String iban, LocalDateTime time, boolean withBalance);

    //============================ Account Details ==============================//

    /**
     * Retrieves a List of AccountDetails by user login (psuId)
     *
     * @param userLogin the user login
     * @return list of account details.
     */
    List<AccountDetailsTO> getAllAccountDetailsByUserLogin(String userLogin);

    /**
     * Retrieves transaction by accountId and transactionId
     *
     * @param accountId     the account id
     * @param transactionId the transaction id
     * @return the corresponding transaction
     */
    TransactionTO getTransactionById(String accountId, String transactionId);

    /**
     * Retrieves a List of transactions by accountId and dates (from/to) if dateTo is empty it is considered that requested date is today
     *
     * @param accountId the account id
     * @param dateFrom  from this time
     * @param dateTo    to this time
     * @return : List of transactions.
     */
    List<TransactionTO> getTransactionsByDates(String accountId, LocalDate dateFrom, LocalDate dateTo);

    /**
     * Retrieves a List of transactions by accountId and dates (from/to) if dateTo is empty it is considered that requested date is today
     *
     * @param accountId the account id
     * @param dateFrom  from this time
     * @param dateTo    to this time
     * @return : List of transactions.
     */
    CustomPageImpl<TransactionTO> getTransactionsByDatesPaged(String accountId, LocalDate dateFrom, LocalDate dateTo, CustomPageableImpl pageable);

    /**
     * Confirm the availability of funds on user account to perform the operation with specified amount
     *
     * @param request : teh fund confirmation request.
     * @return : true if fund available else false.
     */
    boolean confirmFundsAvailability(FundsConfirmationRequestTO request);

    String iban(String id);

    // ======================= CONSENT ======================//

    /**
     * Start an account consent process.
     *
     * @param scaInfoTO  SCA information
     * @param consentId  : the cosent id.
     * @param aisConsent : the consent details
     * @return the corresponding access token describing the account access
     */
    SCAConsentResponseTO startSCA(ScaInfoTO scaInfoTO, String consentId, AisConsentTO aisConsent);

    SCAConsentResponseTO loadSCAForAisConsent(String userId, String consentId, String authorisationId);

    SCAConsentResponseTO selectSCAMethodForAisConsent(String userId, String consentId, String authorisationId, String scaMethodId);

    /**
     * Authorizes a consent request. If the authentication is completed, the returned response will contain a valid bearer token.
     *
     * @param scaInfoTO : SCA information
     * @param consentId : the cosent id
     * @return SCAConsentResponseTO : the consent response.
     */
    SCAConsentResponseTO authorizeConsent(ScaInfoTO scaInfoTO, String consentId);

    /**
     * Provide a third party provider with necessary permission to read accounts and
     * transaction information for the specified account.
     *
     * @param scaInfoTO  : SCA information
     * @param aisConsent : the consent details
     * @return the corresponding access token describing the account access
     */
    SCAConsentResponseTO grantAisConsent(ScaInfoTO scaInfoTO, AisConsentTO aisConsent);

    /**
     * Deposits given amount in cash into specified account.
     * On the bank's books, the bank debits its cash account for the given amount in cash,
     * and credits a "deposits" liability account for an equal amount.
     *
     * @param scaInfoTO SCA information
     * @param accountId id of the account deposited into
     * @param amount    amount of cash deposited
     */
    void depositCash(ScaInfoTO scaInfoTO, String accountId, AmountTO amount);

    /**
     * Retrieves a List of AccountAccessTO by userId
     *
     * @param userId id of the user
     */
    List<AccountAccessTO> getAccountAccesses(String userId);

    /**
     * Remove all transactions for deposit account
     *
     * @param userId    id of the user
     * @param userRole  role of user initiating operation
     * @param accountId the account id
     */
    void deleteTransactions(String userId, UserRoleTO userRole, String accountId);

    void deleteAccount(String userId, UserRoleTO userRole, String accountId);

    void deleteUser(String userId, UserRoleTO userRole, String userToDeleteId);

    AccountReportTO getAccountReport(String accountId);

    boolean changeStatus(String accountId, boolean systemBlock);

}
