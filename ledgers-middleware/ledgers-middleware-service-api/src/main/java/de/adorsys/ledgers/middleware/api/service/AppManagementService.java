package de.adorsys.ledgers.middleware.api.service;

import java.io.IOException;

public interface AppManagementService {

	/**
	 * Called one the application is started to preload the system with 
	 * some data.
	 * 
	 * @throws IOException if the init file can not be found.
	 */
	void initApp() throws IOException;
}
