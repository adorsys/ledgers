package de.adorsys.ledgers.middleware.api.service;

import de.adorsys.ledgers.middleware.api.domain.um.AccountAccessTO;
import de.adorsys.ledgers.middleware.api.domain.um.ScaUserDataTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import de.adorsys.ledgers.middleware.api.exception.UserAlreadyExistsMiddlewareException;
import de.adorsys.ledgers.middleware.api.exception.UserNotFoundMiddlewareException;

import java.util.List;

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
     * Finds user by login
     *
     * @param userLogin users login
     * @return UserTO object
     * @throws UserNotFoundMiddlewareException thrown exception if user is not found
     */
    UserTO findByUserLogin(String userLogin) throws UserNotFoundMiddlewareException;

    /**
     * Update SCA methods by user login
     *
     * @param scaDataList user methods
     * @param userLogin   user login
     */
    UserTO updateScaData(String userLogin, List<ScaUserDataTO> scaDataList) throws UserNotFoundMiddlewareException;

    /**
     * Adds new account for a specific User
     *
     * @param userLogin user login
     * @param accounts  List of Accounts to be added for the user or throws UserNotFoundMiddlewareException
     * @throws UserNotFoundMiddlewareException is thrown if user can`t be found
     */
    UserTO updateAccountAccess(String userLogin, List<AccountAccessTO> accounts) throws UserNotFoundMiddlewareException;

    /**
     * Loads paginated user collection
     *
     * @param page page number
     * @param size size of the page
     * @return list of users
     */
    List<UserTO> listUsers(int page, int size);


    /**
     * Loads list of users by branch and role
     *
     * @param roles user roles
     * @return list of users by branch and role
     */
    List<UserTO> getUsersByBranchAndRoles(String branch, List<UserRoleTO> roles);

    /**
     * Counts users by branch
     *
     * @param branch branch
     * @return amount of users
     */
    int countUsersByBranch(String branch);
}
