package de.adorsys.ledgers.sca.service;

import de.adorsys.ledgers.sca.domain.*;
import de.adorsys.ledgers.um.api.domain.UserBO;

public interface SCAOperationService {

    /**
     * Generates an authentication code, hashes it using the operation data and stores
     * the hash for later verification.
     *
     * @param authCodeData : data for generation auth code
     * @param user         : the user object
     * @param scaStatus    : the actual scaStatus to be set if auth code is generated and sent.
     * @return the generated AuthCode in clear text.
     */
    SCAOperationBO generateAuthCode(AuthCodeDataBO authCodeData, UserBO user, ScaStatusBO scaStatus);

    /**
     * Verify that the auth code, recomputing and verifying the hash of (Auth Code and opData).
     *
     * @param authorisationId : the id of this authorization instance.
     * @param opId            : This is the id of the operation like provided by the consuming module.
     * @param opData          : This are data to be linked to the generated One Time Password.
     * @param authCode        : This auth code was generated at previous step @see #generateAuthCode(String opId, String opData, int validitySeconds)
     * @return SCA validation object
     */
    ScaValidationBO validateAuthCode(String authorisationId, String opId, String opData, String authCode, int scaWeight);


    /**
     * All operations that have status NEW will be changed on EXPIRED if date of creation + validitySeconds in the past,
     * unless validitySeconds is -1;
     */
    void processExpiredOperations();

    /**
     * Creates an authCodeData object.
     *
     * @param authCodeData : data for generation auth code
     * @param scaStatus    : the actual scaStatus to be set.
     * @return the created AuthCode wrapped.
     */
    SCAOperationBO createAuthCode(AuthCodeDataBO authCodeData, ScaStatusBO scaStatus);

    /**
     * Load an auth code data object from the database.
     *
     * @param authorizationId identifier of Authorization object
     * @return SCA Operation object
     */
    SCAOperationBO loadAuthCode(String authorizationId);

    /**
     * load all auth code associated with the given operation id
     *
     * @param opId identifier of primary operation for which authorization is carried
     * @return list of SCA operations
     */
    //List<SCAOperationBO> loadAuthCodesByOpId(String opId);

    /**
     * Return true if all authorization instances of this operation are validated.
     *
     * @param opId   identifier of primary operation for which authorization is carried
     * @param opType type of primary operation
     * @return boolean representation of success or failure
     */
    boolean authenticationCompleted(String opId, OpTypeBO opType);

    /**
     * Verify auth confirmation code
     *
     * @param authorisationId : the id of this authorization instance.
     * @param confirmationCode : Auth confirmation code
     */
    ScaAuthConfirmationBO verifyAuthConfirmationCode(String authorisationId, String confirmationCode);

    /**
     * Compete auth confirmation process
     *
     * @param authorisationId : the id of this authorization instance.
     * @param authCodeConfirmed : Auth confirmation code was successfully confirmed or not
     */
    ScaAuthConfirmationBO completeAuthConfirmation(String authorisationId, boolean authCodeConfirmed);

    SCAOperationBO checkIfExistsOrNew(AuthCodeDataBO data);

    int updateFailedCount(String authorisationId);
}
