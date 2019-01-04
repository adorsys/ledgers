package de.adorsys.ledgers.mockbank.simple.data.test.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.springframework.core.env.Environment;

import de.adorsys.ledgers.mockbank.simple.data.MockbankInitData;
import de.adorsys.ledgers.mockbank.simple.data.test.api.DataUploadService;

/**
 * 
 * Store mockbanks data in the ledgers.
 * 
 * @author bwa
 *
 */
public class DataUploadServiceImpl implements DataUploadService{

	private final Environment env;
	
	public DataUploadServiceImpl(Environment env) {
		super();
		this.env = env;
	}

	/* (non-Javadoc)
	 * @see de.adorsys.ledgers.mockbank.simple.data.test.api.MockBankSimpleDataUploadService#loadData(de.adorsys.ledgers.mockbank.simple.data.MockbankInitData)
	 */
	@Override
	public void loadData(MockbankInitData initData) {
		uploadToRemoteMockBank(initData, buildUrl());
	}

	@SuppressWarnings("PMD.UnusedFormalParameter")
	private void uploadToRemoteMockBank(MockbankInitData initData, String baseUrl) {
		// TODO FIX
//		MockBankSimpleInitService initService = new MockBankSimpleInitService(initData);
//		initService.runInit(baseUrl);
	}

	private String buildUrl() {
		try {
			String ip = InetAddress.getLocalHost().getHostAddress();
			int port = env.getProperty("local.server.port", Integer.class);
			return String.format("http://%s:%d", ip, port);
		} catch (UnknownHostException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}



}
