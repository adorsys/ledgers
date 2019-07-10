/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.ledgers.um.api.service;

import de.adorsys.ledgers.um.api.domain.*;

import java.util.Date;
import java.util.List;

public interface UserService {

    /**
     * Creates a new user
     *
     * @param user User business object
     * @return A persisted user
     */
    UserBO create(UserBO user);

    /**
     * Verify user credential and produces a corresponding login token.
     * <p>
     * The granted access token can no be used to access account information.
     *
     * @param login           User login
     * @param pin             User PIN
     * @param role            the role of this user
     * @param scaId           the scaId
     * @param authorisationId the authorization id
     * @return BearerTokenBO representation of authorization status true for success
     */
    BearerTokenBO authorise(String login, String pin, UserRoleBO role, String scaId, String authorisationId);

    /**
     * Finds a User by its identifier
     *
     * @param id User identifier
     * @return a User
     */
    UserBO findById(String id);

    /**
     * Finds a User by its login
     *
     * @param login User identifier
     * @return a User
     */
    UserBO findByLogin(String login);

    /**
     * Update SCA methods by user login
     *
     * @param scaDataList user methods
     * @param userLogin   user login
     * @return The user object.
     */
    UserBO updateScaData(List<ScaUserDataBO> scaDataList, String userLogin);

    UserBO updateAccountAccess(String userLogin, List<AccountAccessBO> accountAccessListBO);

    List<UserBO> listUsers(int page, int size);

    /**
     * Check if the provided token is valid at the given reference time and return the corresponding user.
     *
     * @param accessToken the access token to validate
     * @param refTime     the reference time
     * @return the bearer token
     */
    BearerTokenBO validate(String accessToken, Date refTime);

    /**
     * Provides a token used to gain read access to an account.
     *
     * @param scaInfoBO SCA information
     * @param aisConsent  the ais consent.
     * @return the bearer token
     */
    BearerTokenBO consentToken(ScaInfoBO scaInfoBO, AisConsentBO aisConsent);

    /**
     * Create a new token for the current user, after a successful auth code process..
     *
     * @param scaInfoBO      : SCA information
     * @return the bearer token
     */
    BearerTokenBO scaToken(ScaInfoBO scaInfoBO);

    /**
     * Create a new token for the current user, with a new authorization id
     *
     * @param scaInfoBO      : SCA information
     * @return the bearer token
     */
    BearerTokenBO loginToken(ScaInfoBO scaInfoBO);

    /**
     * Stores a consent in the consent database and returns the original consent
     * if already existing there.
     *
     * @param consentBO the consent object
     * @return the ais consent stored
     */
    AisConsentBO storeConsent(AisConsentBO consentBO);

    /**
     * Loads a consent given the consent id. Throws a consent not found exception.
     *
     * @param consentId the consent id
     * @return the corresponding ais consent.
     */
    AisConsentBO loadConsent(String consentId);

    /**
     * Loads users collection by branch and the given roles
     *
     * @param branch    branch ID
     * @param userRoles list of user roles
     * @return List of users filtered by branch and user roles
     */
    List<UserBO> findByBranchAndUserRolesIn(String branch, List<UserRoleBO> userRoles);

    /**
     * Counts amount of users for a branch
     *
     * @param branch branch
     * @return amount of users
     */
    int countUsersByBranch(String branch);
}
