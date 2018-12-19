package de.adorsys.ledgers.mockbank.simple.data.test.api;

import java.io.InputStream;

public interface MockBankSimpleDataUploadService {

	/**
	 * Read the yml file inputstream, parse it to a MockbankInitData, save the data to the middleware. 
	 * @param dataInputStream
	 */
	void loadData(InputStream dataInputStream);

}
