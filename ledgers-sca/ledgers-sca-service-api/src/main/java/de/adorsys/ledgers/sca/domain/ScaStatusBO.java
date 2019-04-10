package de.adorsys.ledgers.sca.domain;

/**
 * The status of the sca process...
 * 
 * @author fpo
 *
 */
public enum ScaStatusBO {
	RECEIVED, 
	PSUIDENTIFIED, 
	PSUAUTHENTICATED,
	SCAMETHODSELECTED, 
	STARTED, 
	FINALISED, 
	FAILED,
	EXEMPTED
}
