/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.postings.api.domain;


public enum PostingStatusBO {
	/*Deferred*/
	DEFERRED,
	/*Posted*/
	POSTED,
	/*proposed*/
	PROPOSED,
	/*simulated*/
	SIMULATED,
	/*tax*/
	TAX,
	/*unposted*/
	UNPOSTED,
	/*cancelled*/
	CANCELLED,
	/*other*/
	OTHER
	
}
