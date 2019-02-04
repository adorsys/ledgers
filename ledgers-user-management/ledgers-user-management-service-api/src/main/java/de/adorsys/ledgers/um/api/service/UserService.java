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

import de.adorsys.ledgers.um.api.domain.AccessTokenBO;
import de.adorsys.ledgers.um.api.domain.AccountAccessBO;
import de.adorsys.ledgers.um.api.domain.AisConsentBO;
import de.adorsys.ledgers.um.api.domain.BearerTokenBO;
import de.adorsys.ledgers.um.api.domain.ScaUserDataBO;
import de.adorsys.ledgers.um.api.domain.UserBO;
import de.adorsys.ledgers.um.api.domain.UserRoleBO;
import de.adorsys.ledgers.um.api.exception.ConsentNotFoundException;
import de.adorsys.ledgers.um.api.exception.InsufficientPermissionException;
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
     * Verify user credential and produces a corresponding login token.
     * 
     * The granted access token can no be used to access account information.
     *
     * @param login User login
     * @param pin   User PIN
     * @param role the role of this user 
     * @param scaId the scaId
     * @param authorisationId the authorization id
     * 
     * @return BearerTokenBO representation of authorization status true for success, false for failure or throws a UserNotFoundException
     * @throws UserNotFoundException is thrown if user can`t be found
     * @throws InsufficientPermissionException usder does not have requested role
     */
    BearerTokenBO authorise(String login, String pin, UserRoleBO role, String scaId, String authorisationId) throws UserNotFoundException, InsufficientPermissionException;

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
     * @return The user object.
     * @throws UserNotFoundException no user with given login.
     */
    UserBO updateScaData(List<ScaUserDataBO> scaDataList, String userLogin) throws UserNotFoundException;

    UserBO updateAccountAccess(String userLogin, List<AccountAccessBO> accountAccessListBO) throws UserNotFoundException;

    List<UserBO> listUsers(int page, int size);

	List<UserBO> getAll();

	/**
	 * Check if the provided token is valid at the given reference time and return the corresponding user.
	 * 
	 * 
	 * @param accessToken the access token to validate
	 * @param refTime the reference time
	 * @return the bearer token
	 * @throws UserNotFoundException : user is no longer in the database.
	 * @throws InsufficientPermissionException access permissions contained in the token are no longer supported by the user.
	 */
	BearerTokenBO validate(String accessToken, Date refTime) throws UserNotFoundException, InsufficientPermissionException;

	/**
	 * Provides a token used to gain read access to an account.
	 * 
	 * @param accessToken the token used by the user currently granting permission
	 * @param aisConsent the ais consent.
	 * @return the bearer token
	 * @throws InsufficientPermissionException the current user does not have sufficient permission.
	 */
	BearerTokenBO consentToken(AccessTokenBO accessToken, AisConsentBO aisConsent) throws InsufficientPermissionException;

	/**
	 * Create a new token for the current user, after a successfull auth code proces..
	 * 
	 * @param loginToken : the token obtained in the preceeding request.
	 * @return the bearer token
	 * @throws InsufficientPermissionException : user does not have the required role.
	 * @throws UserNotFoundException : underlying user non longer existent. 
	 */
	BearerTokenBO scaToken(AccessTokenBO loginToken) throws InsufficientPermissionException, UserNotFoundException;

	/**
	 * Create a new token for the current user, with a new authorization id
	 * 
	 * @param loginToken : the token to clone
	 * @param authorisationId : the authorization id to exchange
	 * @return the bearer token
	 * @throws InsufficientPermissionException : user does not have the required role.
	 * @throws UserNotFoundException : underlying user non longer existent. 
	 */
	BearerTokenBO loginToken(AccessTokenBO loginToken, String authorisationId) throws InsufficientPermissionException, UserNotFoundException;
	
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
	 * @throws ConsentNotFoundException
	 */
	AisConsentBO loadConsent(String consentId) throws ConsentNotFoundException;

	/**
	 * Loads users collection by branch and the given roles
	 *
	 * @param branch branch ID
	 * @param userRoles list of user roles
	 * @return List of users filtered by branch and user roles
	 */
	List<UserBO> findByBranchAndUserRolesIn(String branch, List<UserRoleBO> userRoles);
}
