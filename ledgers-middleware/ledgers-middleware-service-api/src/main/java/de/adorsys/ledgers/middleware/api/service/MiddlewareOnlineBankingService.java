package de.adorsys.ledgers.middleware.api.service;

import de.adorsys.ledgers.middleware.api.domain.sca.SCALoginResponseTO;
import de.adorsys.ledgers.middleware.api.domain.um.BearerTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.exception.AuthCodeGenerationMiddlewareException;
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
	 * @return
	 * @throws UserAlreadyExistsMiddlewareException
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
	 *         UserNotFoundMiddlewareException
	 * @throws UserNotFoundMiddlewareException is thrown if user can`t be found
	 * @throws InsufficientPermissionMiddlewareException 
	 */
	SCALoginResponseTO authorise(String login, String pin, UserRoleTO role) throws UserNotFoundMiddlewareException, InsufficientPermissionMiddlewareException;

	/**
	 * Caller can be sure that returned user object contains a mirror of permissions
	 * contained in the token. This is generally a subset of permissions really held
	 * by the user. If during validation we notice that the user has less permission
	 * for the listed account, the token will be discarded an no user object will be
	 * returned.
	 * 
	 * @param accessToken
	 * @return
	 * @throws UserNotFoundMiddlewareException
	 */
	BearerTokenTO validate(String accessToken) throws UserNotFoundMiddlewareException;
	
    // ================= SCA =======================================//

    /**
     * <p>
     * After the PSU selects the SCA method, this is called to generate and send the login auth code.
     *
     * @param authCodeData Data that needed for auth code generation
     * @return SCALoginResponseTO the response object.
     * @throws SCAOperationValidationMiddlewareException 
     * @throws AuthCodeGenerationMiddlewareException  if something happens during auth code generation
     * @throws SCAMethodNotSupportedMiddleException   if user sca method doesn't support by ledgers
     * @throws UserNotFoundMiddlewareException        if user not found by id
     * @throws UserScaDataNotFoundMiddlewareException if sca user data not found by id
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
     * @param opId
     * @param authCode
     * @return
     * @throws SCAOperationNotFoundMiddlewareException
     * @throws SCAOperationValidationMiddlewareException
     * @throws SCAOperationExpiredMiddlewareException
     * @throws SCAOperationUsedOrStolenMiddlewareException
     * @throws InsufficientPermissionMiddlewareException 
     */
	SCALoginResponseTO authenticateForLogin(String authorisationId, String authCode)
			throws SCAOperationNotFoundMiddlewareException, SCAOperationValidationMiddlewareException,
			SCAOperationExpiredMiddlewareException, SCAOperationUsedOrStolenMiddlewareException, 
			InsufficientPermissionMiddlewareException;
	
}
