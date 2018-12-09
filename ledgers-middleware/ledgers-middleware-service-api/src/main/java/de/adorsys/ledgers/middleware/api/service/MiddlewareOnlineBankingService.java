package de.adorsys.ledgers.middleware.api.service;

import de.adorsys.ledgers.middleware.api.domain.um.AccessTokenTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.exception.InsufficientPermissionMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.UserAlreadyExistsMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.UserNotFoundMiddlewareException;

/**
 * Interface used for the initialization of user interaction. Implementation of
 * this interface will generally not require user interaction.
 * 
 * @author fpo
 *
 */
public interface MiddlewareOnlineBankingService {
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
	String authorise(String login, String pin, UserRoleTO role) throws UserNotFoundMiddlewareException, InsufficientPermissionMiddlewareException;

	/**
	 * Caller can be sure that returned user object contains a miror of permissions
	 * contained in the token. This is generally a subset of permissions realy held
	 * by the user. If during validation we notice that the user has less permission
	 * for the listed account, the token will be discarded an no user object will be
	 * returned.
	 * 
	 * @param accessToken
	 * @return
	 * @throws UserNotFoundMiddlewareException
	 */
	AccessTokenTO validate(String accessToken) throws UserNotFoundMiddlewareException;

	/**
	 * Registers a new User.
	 * 
	 * @param login the login of the user
	 * @param email the email of the user
	 * @param pin the pin of this user
	 * @param role the initial role of the user.
	 * @return
	 * @throws UserAlreadyExistsMiddlewareException
	 */
	UserTO register(String login, String email, String pin, UserRoleTO role) throws UserAlreadyExistsMiddlewareException;
}
