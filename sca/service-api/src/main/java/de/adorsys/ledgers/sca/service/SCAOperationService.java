package de.adorsys.ledgers.sca.service;

public interface SCAOperationService {
	/**
	 * Generates an authentication code, hash it using the operation data and stores
	 * the hash for later verification.
	 * 
	 * @param opId : This is the id of the operation like provided by the consuming module.
	 * @param opData : This are data to be linked to the generated One Time Password.
	 * @param validitySeconds : documents the validity time of the generated OTP
	 * @return the generated AuthCode in clear text.
	 */
	String generateAuthCode(String opId, String opData, int validitySeconds);
	/**
	 * Verify that the auth code, recomputing and verifying the hash of (Auth Code and opData).
	 *  
	 * @param opId
	 * @param opData
	 * @param authCode
	 * @return
	 */
	boolean validateAuthCode(String opId, String opData, String authCode);
}
