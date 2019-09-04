package de.adorsys.ledgers.middleware.api.service;

public interface AppManagementService {

	/**
	 * Called one the application is started to preload the system with 
	 * some data.
	 */
	void initApp();

	void removeBranch(String branchId);
}
