/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.sca.domain;

/**
 * The status of the sca process...
 * 
 * @author fpo
 *
 */
public enum ScaStatusBO {
	RECEIVED, //NOT USED
	PSUIDENTIFIED, //NOT USED
	PSUAUTHENTICATED,
	SCAMETHODSELECTED, //METHOD SELECTED
	STARTED, //NOT USED
	FINALISED, //SCA COMPLETED
	FAILED,
	EXEMPTED, //NO SCA REQUIRED
	UNCONFIRMED;
}
