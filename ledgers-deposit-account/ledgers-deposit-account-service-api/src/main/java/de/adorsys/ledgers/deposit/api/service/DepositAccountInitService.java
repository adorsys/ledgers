package de.adorsys.ledgers.deposit.api.service;

import java.io.IOException;

/**
 * Start initialization of deposit account module after environment initialized.
 * 
 * @author fpo
 *
 */
public interface DepositAccountInitService {

	void initConfigData() throws IOException;

}
