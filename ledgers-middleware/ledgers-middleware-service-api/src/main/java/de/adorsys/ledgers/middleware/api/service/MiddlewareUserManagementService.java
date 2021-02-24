package de.adorsys.ledgers.middleware.api.service;

import de.adorsys.ledgers.middleware.api.domain.account.AccountIdentifierTypeTO;
import de.adorsys.ledgers.middleware.api.domain.account.AccountReferenceTO;
import de.adorsys.ledgers.middleware.api.domain.account.AdditionalAccountInformationTO;
import de.adorsys.ledgers.middleware.api.domain.sca.ScaInfoTO;
import de.adorsys.ledgers.middleware.api.domain.um.*;
import de.adorsys.ledgers.util.domain.CustomPageImpl;
import de.adorsys.ledgers.util.domain.CustomPageableImpl;

import java.util.List;

public interface MiddlewareUserManagementService {
    /**
     * Creates a new user
     *
     * @param user User transfer object
     * @return A persisted user
     */
    UserTO create(UserTO user);

    /**
     * Finds a User by its identifier
     *
     * @param id User identifier
     * @return a User
     */
    UserTO findById(String id);

    /**
     * Finds user by login
     *
     * @param userLogin users login
     * @return UserTO object
     */
    UserTO findByUserLogin(String userLogin);

    /**
     * Update SCA methods by user login
     *
     * @param scaDataList user methods
     * @param userLogin   user login
     */
    UserTO updateScaData(String userLogin, List<ScaUserDataTO> scaDataList);

    /**
     * Adds new account for a specific User
     *
     * @param scaInfo container for TPP data from access token
     * @param userId  user id
     * @param access  Access to an account
     */
    void updateAccountAccess(ScaInfoTO scaInfo, String userId, AccountAccessTO access);

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
     * @param countryCode Country Code
     * @param roles       user roles
     * @return list of users by branch and role
     */
    CustomPageImpl<UserTO> getUsersByBranchAndRoles(String countryCode, String branchId, String branchLogin, String userLogin, List<UserRoleTO> roles, Boolean blocked, CustomPageableImpl pageable);

    /**
     * Returns list of logins for users (role = CUSTOMER) within the given branch.
     *
     * @param branch branch ID.
     * @return list of logins.
     */
    List<String> getBranchUserLogins(String branch);

    CustomPageImpl<UserTO> getUsersByRoles(List<UserRoleTO> roles, CustomPageableImpl pageable);

    /**
     * Loads list of users by branch and role
     *
     * @param countryCode Country Code
     * @param roles       user roles
     * @return list of users by branch and role
     */
    CustomPageImpl<UserExtendedTO> getUsersByBranchAndRolesExtended(String countryCode, String branchId, String branchLogin, String userLogin, List<UserRoleTO> roles, Boolean blocked, CustomPageableImpl pageable);

    /**
     * Counts users by branch
     *
     * @param branch branch
     * @return amount of users
     */
    int countUsersByBranch(String branch);

    UserTO updateUser(String branchId, UserTO user);

    void updatePasswordIfRequired(String userId, String password);

    boolean checkMultilevelScaRequired(String login, String iban);

    boolean checkMultilevelScaRequired(String login, List<AccountReferenceTO> references);

    List<AdditionalAccountInformationTO> getAdditionalInformation(ScaInfoTO scaInfoHolder, AccountIdentifierTypeTO accountIdentifierType, String accountIdentifier);

    boolean changeStatus(String userId, boolean systemBlock);

    void editBasicSelf(String userId, UserTO user);

    void revertDatabase(String userId, long recoveryPointId);

    void resetPasswordViaEmail(String login);

    String findAccountOwner(String accountId);
}
