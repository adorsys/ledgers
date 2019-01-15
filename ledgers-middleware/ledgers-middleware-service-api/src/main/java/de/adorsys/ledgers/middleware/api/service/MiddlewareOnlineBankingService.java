package de.adorsys.ledgers.middleware.api.service;

import de.adorsys.ledgers.middleware.api.domain.sca.OpTypeTO;
import de.adorsys.ledgers.middleware.api.domain.sca.SCALoginResponseTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.exception.InsufficientPermissionMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.SCAMethodNotSupportedMiddleException;
import de.adorsys.ledgers.middleware.api.exception.SCAOperationExpiredMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.SCAOperationNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.SCAOperationUsedOrStolenMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.SCAOperationValidationMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.UserAlreadyExistsMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.UserNotFoundMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.UserScaDataNotFoundMiddlewareException;

/**
 * Interface used for the initialization of user interaction. Implementation of
 * this interface will generally not require user interaction.
 * 
 * @author fpo
 *
 */
public interface MiddlewareOnlineBankingService {

	/**
	 * Registers a User.
	 * 
	 * @param login the login of the user
	 * @param email the email of the user
	 * @param pin the pin of this user
	 * @param role the initial role of the user.
	 * @return : user
	 * @throws UserAlreadyExistsMiddlewareException : user with login exists.
	 */
	UserTO register(String login, String email, String pin, UserRoleTO role) throws UserAlreadyExistsMiddlewareException;
	
	/**
	 * Performs user authorization.
	 * 
	 * The returned String will be a signed JWT containing user access rights and
	 * sca data.
	 *
	 * @param login User login
	 * @param pin   User PIN
	 * @param role The intended role.
	 * 
	 * @return a session id for success, false for failure or trows a
	 * @throws UserNotFoundMiddlewareException :sis thrown if user can`t be found
	 * @throws InsufficientPermissionMiddlewareException  : permission not sufficient
	 */
	SCALoginResponseTO authorise(String login, String pin, UserRoleTO role) throws UserNotFoundMiddlewareException, InsufficientPermissionMiddlewareException;

	/**
	 * Special login associated with a account information, a payment or a payment cancellation consent.
	 * 
	 * @param login the login of the customer
	 * @param pin the password of the customer
	 * @param consentId the consentId or paymentId
	 * @param authorisationId the authorisationId
	 * @param opType the operation type
	 * @return
	 * @throws UserNotFoundMiddlewareException :sis thrown if user can`t be found
	 * @throws InsufficientPermissionMiddlewareException  : permission not sufficient
	 */
	SCALoginResponseTO authoriseForConsent(String login, String pin, String consentId, String authorisationId,
			OpTypeTO opType) throws UserNotFoundMiddlewareException, InsufficientPermissionMiddlewareException;

	/**
	 * Caller can be sure that returned user object contains a mirror of permissions
	 * contained in the token. This is generally a subset of permissions really held
	 * by the user. If during validation we notice that the user has less permission
	 * for the listed account, the token will be discarded an no user object will be
	 * returned.
	 * 
	 * @param accessToken : the access token
	 * @return the bearer token
	 * @throws UserNotFoundMiddlewareException : user not found in db.
	 * @throws InsufficientPermissionMiddlewareException : token contains more permission than the assigned to the user in the database.
	 */
	BearerTokenTO validate(String accessToken) throws UserNotFoundMiddlewareException, InsufficientPermissionMiddlewareException;
	
    // ================= SCA =======================================//

    /**
     * <p>
     * After the PSU selects the SCA method, this is called to generate and send the login auth code.
     *
     * @param scaUserDataId scaMethod
     * @param authorisationId the id of the auth process
     * @param userMessage message to user
     * @param validitySeconds validity in secondn.
     * @return SCALoginResponseTO the response object.
     * 
     * @throws SCAOperationNotFoundMiddlewareException op not found
     * @throws InsufficientPermissionMiddlewareException : missing permissions
     * @throws SCAMethodNotSupportedMiddleException   if user sca method doesn't support by ledgers
     * @throws UserScaDataNotFoundMiddlewareException if sca user data not found by id
     * @throws SCAOperationValidationMiddlewareException : inputs not valid
     */
	SCALoginResponseTO generateLoginAuthCode(String scaUserDataId, String authorisationId, String userMessage,
			int validitySeconds) throws SCAOperationNotFoundMiddlewareException, 
    		InsufficientPermissionMiddlewareException, SCAMethodNotSupportedMiddleException, 
    		UserScaDataNotFoundMiddlewareException, SCAOperationValidationMiddlewareException;

    /**
     * PROC: 02c
     * <p>
     * This is called when the user enters the received code.
     * 
     * @param authorisationId : the operation id
     * @param authCode the auth code.
     * @return the login response.
     * @throws SCAOperationNotFoundMiddlewareException : operation not found.
     * @throws SCAOperationValidationMiddlewareException : input data not valid.
     * @throws SCAOperationExpiredMiddlewareException : expired
     * @throws SCAOperationUsedOrStolenMiddlewareException : malicious input
     * @throws InsufficientPermissionMiddlewareException  : not enough permissions.
     */
	SCALoginResponseTO authenticateForLogin(String authorisationId, String authCode)
			throws SCAOperationNotFoundMiddlewareException, SCAOperationValidationMiddlewareException,
			SCAOperationExpiredMiddlewareException, SCAOperationUsedOrStolenMiddlewareException, 
			InsufficientPermissionMiddlewareException;

}
