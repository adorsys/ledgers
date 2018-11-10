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

import de.adorsys.ledgers.um.api.domain.ScaUserDataBO;
import de.adorsys.ledgers.um.api.domain.AccountAccessBO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.exception.UserAlreadyExistsException;
import de.adorsys.ledgers.um.api.exception.UserNotFoundException;
import java.util.List;

public interface UserService {

    /**
     * Creates a new user
     *
     * @param user User business object
     * @return A persisted user or trows a UserAlreadyExistsException
     * @throws UserAlreadyExistsException is thrown if user already exists
     */
    UserBO create(UserBO user) throws UserAlreadyExistsException;

    /**
     * Performs user authorisation
     *
     * @param id  User identifier
     * @param pin User PIN
     * @return Boolean representation of authorisation status true for success, false for failure or trows a UserNotFoundException
     * @throws UserNotFoundException is thrown if user can`t be found
     */
    boolean authorize(String id, String pin) throws UserNotFoundException;

    /**
     * Performs user authorisation
     *
     * @param id        User identifier
     * @param pin       User PIN
     * @param accountId Account identifier
     * @return Boolean representation of authorisation status true for success, false for failure or trows a UserNotFoundException
     * @throws UserNotFoundException is thrown if user can`t be found
     */
    boolean authorize(String id, String pin, String accountId) throws UserNotFoundException;

    /**
     * Adds new account for a specific User
     *
     * @param login   User login
     * @param account Account to added for the user or throws UserNotFoundException
     * @throws UserNotFoundException is thrown if user can`t be found
     */
//    void addAccount(String login, LedgerAccount account) throws UserNotFoundException;

    /**
     * Finds a User by its identifier
     *
     * @param id User identifier
     * @return a User or throws a UserNotFoundException
     * @throws UserNotFoundException is thrown if user can`t be found
     */
    UserBO findById(String id) throws UserNotFoundException;

    /**
     *
     * @param userId user ID
     * @return List<ScaUserDataBO> collection of SCAs for a user
     * @throws UserNotFoundException is thrown if user can`t be found
     */
    List<ScaUserDataBO> getUserScaData(String userId) throws UserNotFoundException;

    /**
     *
     * @param userId user ID
     * @return List<AccountAccessBO> collection of AccountAccesses for a user
     * @throws UserNotFoundException is thrown if user can`t be found
     */
    List<AccountAccessBO> getAccountAccess(String userId) throws UserNotFoundException;

    /**
     *
     * @param userLogin user login
     * @return List<AccountAccessBO> collection of AccountAccesses for a user
     * @throws UserNotFoundException is thrown if user can`t be found
     */
    List<AccountAccessBO> getAccountAccessByUserLogin(String userLogin) throws UserNotFoundException;

    /**
     * Update SCA methods by user login
     *
     * @param scaDataList user methods
     * @param userLogin user login
     */
    void updateScaData(List<ScaUserDataBO> scaDataList, String userLogin) throws UserNotFoundException;
}
