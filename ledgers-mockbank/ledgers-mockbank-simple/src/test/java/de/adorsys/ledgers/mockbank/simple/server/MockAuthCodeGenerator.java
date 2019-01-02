package de.adorsys.ledgers.mockbank.simple.server;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import de.adorsys.ledgers.sca.service.AuthCodeGenerator;

@Service
@Primary
public class MockAuthCodeGenerator implements AuthCodeGenerator {
	String testAuthCode = System.getProperty("de.adorsys.ledgers.sca.service.AuthCodeGenerator.testAuthCode", "123456");
	@Override
	public String generate() {
		return testAuthCode;
	}
	

}
