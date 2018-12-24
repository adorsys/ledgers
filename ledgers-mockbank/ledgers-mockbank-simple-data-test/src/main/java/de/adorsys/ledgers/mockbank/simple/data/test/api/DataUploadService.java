package de.adorsys.ledgers.mockbank.simple.data.test.api;

import de.adorsys.ledgers.mockbank.simple.data.MockbankInitData;

public interface DataUploadService {

	/**
	 * Read the yml file inputstream, parse it to a MockbankInitData, save the data to the middleware. 
	 * @param initData a MockbankInitData instance.
	 */
	void loadData(MockbankInitData initData);

}
