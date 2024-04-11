/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.sca.db.domain;

/**
 * The status of the sca process...
 * 
 * @author fpo
 *
 */
public enum ScaStatus {
	RECEIVED, 
	PSUIDENTIFIED, 
	PSUAUTHENTICATED,
	SCAMETHODSELECTED, 
	STARTED, 
	FINALISED, 
	FAILED,
	EXEMPTED,
	UNCONFIRMED;
}
