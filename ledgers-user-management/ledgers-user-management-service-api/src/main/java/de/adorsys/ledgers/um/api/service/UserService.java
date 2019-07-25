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
