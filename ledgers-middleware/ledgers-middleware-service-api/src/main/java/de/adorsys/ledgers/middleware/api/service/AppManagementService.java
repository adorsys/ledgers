package de.adorsys.ledgers.middleware.api.service;

import java.io.IOException;

public interface AppManagementService {

	/**
	 * Called one the application is started to preload the system with 
	 * some data.
	 * 
	 * @throws IOException
	 */
	void initApp() throws IOException;
}
