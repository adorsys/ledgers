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

import java.util.Date;
import java.util.List;

import de.adorsys.ledgers.um.api.domain.AccountAccessBO;
import de.adorsys.ledgers.um.api.domain.ScaUserDataBO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.exception.UserAlreadyExistsException;
import de.adorsys.ledgers.um.api.exception.UserNotFoundException;

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
     * @param login User login
     * @param pin   User PIN
     * @return Boolean representation of authorisation status true for success, false for failure or trows a UserNotFoundException
     * @throws UserNotFoundException is thrown if user can`t be found
     */
    String authorise(String login, String pin) throws UserNotFoundException;

    /**
     * Performs user authorisation
     *
     * @param id        User identifier
     * @param pin       User PIN
     * @param accountId Account identifier
     * @return String representation of authorisation token for success, false for failure or trows a UserNotFoundException
     * @throws UserNotFoundException is thrown if user can`t be found
     */
    String authorise(String id, String pin, String accountId) throws UserNotFoundException;

    /**
     * Finds a User by its identifier
     *
     * @param id User identifier
     * @return a User or throws a UserNotFoundException
     * @throws UserNotFoundException is thrown if user can`t be found
     */
    UserBO findById(String id) throws UserNotFoundException;

    /**
     * Finds a User by its login
     *
     * @param login User identifier
     * @return a User or throws a UserNotFoundException
     * @throws UserNotFoundException is thrown if user can`t be found
     */
    UserBO findByLogin(String login) throws UserNotFoundException;

    /**
     * Update SCA methods by user login
     *
     * @param scaDataList user methods
     * @param userLogin   user login
     * @return
     */
    UserBO updateScaData(List<ScaUserDataBO> scaDataList, String userLogin) throws UserNotFoundException;

    UserBO updateAccountAccess(String userLogin, List<AccountAccessBO> accountAccessListBO) throws UserNotFoundException;

    List<UserBO> listUsers(int page, int size);

	/**
	 * Check if the provided token is valid at the given reference time and return the corresponding user.
	 * 
	 * 
	 * @param accessToken
	 * @param refTime
	 * @return
	 * @throws UserNotFoundException 
	 */
	UserBO validate(String accessToken, Date refTime) throws UserNotFoundException;
}
