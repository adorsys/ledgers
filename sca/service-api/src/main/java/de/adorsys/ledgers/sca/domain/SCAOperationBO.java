package de.adorsys.ledgers.sca.domain;

import java.time.LocalDateTime;

/**
 * Documentation for impl.
 * @author fpo
 *
 */
public class SCAOperationBO {
	String opId;
	int validitySeconds;
	/*The hash of auth code and opData*/
	String authCodeHash;
	AuthCodeStatus status;
	String hashAlg;
	LocalDateTime created;
	LocalDateTime statusTime;
}
