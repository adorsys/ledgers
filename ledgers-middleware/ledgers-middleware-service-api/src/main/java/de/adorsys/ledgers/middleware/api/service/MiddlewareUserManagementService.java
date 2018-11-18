package de.adorsys.ledgers.middleware.api.service;

import java.util.List;

import de.adorsys.ledgers.middleware.api.domain.um.AccountAccessTO;
import de.adorsys.ledgers.middleware.api.domain.um.ScaUserDataTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.exception.UserAlreadyExistsMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.UserNotFoundMiddlewareException;

public interface MiddlewareUserManagementService {
    /**
     * Creates a new user
     *
     * @param user User transfer object
     * @return A persisted user or trows a UserAlreadyExistsException
     * @throws UserAlreadyExistsMiddlewareException is thrown if user already exists
     */
    UserTO create(UserTO user) throws UserAlreadyExistsMiddlewareException;

    /**
     * Finds a User by its identifier
     *
     * @param id User identifier
     * @return a User or throws a UserNotFoundMiddlewareException
     * @throws UserNotFoundMiddlewareException is thrown if user can`t be found
     */
    UserTO findById(String id) throws UserNotFoundMiddlewareException;

    /**
     *
     * @param userLogin user login
     * @return List<AccountAccessBO> collection of AccountAccesses for a user
     * @throws UserNotFoundMiddlewareException is thrown if user can`t be found
     */
    UserTO findByUserLogin(String userLogin) throws UserNotFoundMiddlewareException;

    /**
     * Update SCA methods by user login
     *
     * @param scaDataList user methods
     * @param userLogin user login
     */
    UserTO updateScaData(String userLogin, List<ScaUserDataTO> scaDataList) throws UserNotFoundMiddlewareException;

    /**
     * Adds new account for a specific User
     *
     * @param login   User login
     * @param account Account to added for the user or throws UserNotFoundMiddlewareException
     * @throws UserNotFoundMiddlewareException is thrown if user can`t be found
     */
    UserTO updateAccountAccess(String userLogin, List<AccountAccessTO> accounts) throws UserNotFoundMiddlewareException;
    
	List<UserTO> listUsers(int page, int size);
	
    /**
     * Performs user authorisation
     *
     * @param login  User login
     * @param pin User PIN
     * @return Boolean representation of authorisation status true for success, false for failure or trows a UserNotFoundException
     * @throws UserNotFoundMiddlewareException is thrown if user can`t be found
     */
    boolean authorise(String login, String pin) throws UserNotFoundMiddlewareException;

}
