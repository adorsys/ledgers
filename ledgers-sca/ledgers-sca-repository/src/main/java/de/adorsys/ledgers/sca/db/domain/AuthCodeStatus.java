/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.sca.db.domain;

public enum AuthCodeStatus {
	/*
	 * The authorization process is created, but the authorization code was not sent to the user.
	 * 
	 * This occurs, when the operation creates authorization requirement at initialization.
	 * 
	 */
	INITIATED,
	/*
	 * The authorization code was generated and sent to the user. From the
	 * moment the code is sent, expiration starts counting.
	 */
	SENT,
	/*
	 * The user successfully validated the authorization code.
	 */
	VALIDATED, 
	/*
	 * The validation of the code failed.
	 */
	FAILED,
	/*
	 * The code is expired.
	 */
	EXPIRED, 
	/*
	 * The underlying process is executed. Entries can be removed from the database.
	 */
	DONE;
}
