package de.adorsys.ledgers.sca.service;

import de.adorsys.ledgers.sca.domain.AuthCodeDataBO;
import de.adorsys.ledgers.sca.domain.OpTypeBO;
import de.adorsys.ledgers.sca.domain.SCAOperationBO;
import de.adorsys.ledgers.sca.domain.ScaStatusBO;
import de.adorsys.ledgers.sca.exception.*;
import de.adorsys.ledgers.um.api.domain.UserBO;

public interface SCAOperationService {

	/**
	 * Generates an authentication code, hashes it using the operation data and stores
	 * the hash for later verification.
	 *
	 * @param authCodeData : data for generation auth code
	 * @param user : the user object
	 * @param scaStatus : the actual scaStatus to be set if auth code is generated and sent.
	 * @return the generated AuthCode in clear text.
	 * @throws SCAMethodNotSupportedException
	 * @throws SCAOperationNotFoundException
	 * @throws SCAOperationValidationException 
	 */
	SCAOperationBO generateAuthCode(AuthCodeDataBO authCodeData, UserBO user, ScaStatusBO scaStatus) throws SCAMethodNotSupportedException, SCAOperationValidationException, SCAOperationNotFoundException;

	/**
	 * Verify that the auth code, recomputing and verifying the hash of (Auth Code and opData).
	 *  
	 * @param authorisationId : the id of this authorization instance.
	 * @param opId : This is the id of the operation like provided by the consuming module.
	 * @param opData : This are data to be linked to the generated One Time Password.
	 * @param authCode : This auth code was generated at previous step @see #generateAuthCode(String opId, String opData, int validitySeconds)
	 * @return true if auth code is valid in other cases false will be returned
	 * @throws SCAOperationNotFoundException
	 * @throws SCAOperationValidationException
	 * @throws SCAOperationUsedOrStolenException
	 * @throws SCAOperationExpiredException
	 */
	boolean validateAuthCode(String authorisationId, String opId, String opData, String authCode, int scaWeight) throws SCAOperationNotFoundException, SCAOperationValidationException, SCAOperationUsedOrStolenException, SCAOperationExpiredException;


    /**
     * All operations that have status NEW will be changed on EXPIRED if date of creation + validitySeconds in the past,
     * unless validitySeconds is -1;
     */
	void processExpiredOperations();
	
	/**
	 * Creates an authCodeData object.
	 *
	 * @param authCodeData : data for generation auth code
	 * @param scaStatus : the actual scaStatus to be set.
	 * @return the created AuthCode wrapped.
	 */
	SCAOperationBO createAuthCode(AuthCodeDataBO authCodeData, ScaStatusBO scaStatus);
	
	/**
	 * Load an auth code data object from the database.
	 * 
	 * @param authorizationId
	 * @return
	 * @throws SCAOperationNotFoundException
	 */
	SCAOperationBO loadAuthCode(String authorizationId) throws SCAOperationNotFoundException;
	
	/**
	 * load all auth code associated with the given operation id
	 * 
	 * @param opId
	 * @return
	List<SCAOperationBO> loadAuthCodesByOpId(String opId);
	 */

	/**
	 * Return true if all authorization instances of this operation are validated.
	 * @param opId
	 * @param opType
	 * @return
	 */
	boolean authenticationCompleted(String opId, OpTypeBO opType);
}
