/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.api.domain.sca;

/**
 * This interface has to be implemented by any object that has to be stored 
 * by a ledgers connector in the consent database of xs2a.
 * 
 * @author fpo
 *
 */
public interface AspspConsentDataSerial {
	/**
	 * Type of object stored. Might be used to deserialize 
	 * the object. 
	 * 
	 * @return
	 */
	String getObjectType();
}
