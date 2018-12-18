package de.adorsys.ledgers.mockbank.simple.data.test.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import de.adorsys.ledgers.mockbank.simple.MockBankSimpleInitService;
import de.adorsys.ledgers.mockbank.simple.data.MockbankInitData;
import de.adorsys.ledgers.mockbank.simple.data.test.api.MockBankSimpleDataUploadService;
import pro.javatar.commons.reader.YamlReader;

/**
 * 
 * Store mockbanks data in the ledgers.
 * 
 * @author bwa
 *
 */
@Service
public class MockBankSimpleDataUploadServiceImpl implements MockBankSimpleDataUploadService{

	private final Environment env;
	
	public MockBankSimpleDataUploadServiceImpl(Environment env) {
		super();
		this.env = env;
	}

	@Override
	public void loadData(InputStream dataInputStream) {
		try {
			this.loadDataInternal(dataInputStream);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
	
	private void loadDataInternal(InputStream dataInputStream)
			throws UnknownHostException, IOException, JsonParseException, JsonMappingException {
		String ip = InetAddress.getLocalHost().getHostAddress();
		int port = env.getProperty("local.server.port", Integer.class);
		String baseUrl = String.format("http://%s:%d", ip, port);
		
		MockbankInitData mockBankData = YamlReader.getInstance().getObjectFromInputStream(dataInputStream, MockbankInitData.class);
		
		MockBankSimpleInitService initService = new MockBankSimpleInitService(mockBankData);
		
		initService.runInit(baseUrl);
	}

}
