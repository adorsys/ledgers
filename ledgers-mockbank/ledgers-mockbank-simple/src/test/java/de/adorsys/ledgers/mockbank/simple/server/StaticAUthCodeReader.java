package de.adorsys.ledgers.mockbank.simple.server;

import org.springframework.stereotype.Service;

import de.adorsys.ledgers.mockbank.simple.service.AuthCodeReader;

@Service
public class StaticAUthCodeReader implements AuthCodeReader {
	String testAuthCode = System.getProperty("de.adorsys.ledgers.sca.service.AuthCodeGenerator.testAuthCode", "123456");

	@Override
	public String readAuthCode(String opId, String authorizationId) {
		return testAuthCode;
	}

}
