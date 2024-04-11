/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.deposit.api.service;

/**
 * Start initialization of deposit account module after environment initialized.
 * 
 * @author fpo
 *
 */
public interface DepositAccountInitService {

	void initConfigData();

}
