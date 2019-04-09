package de.adorsys.ledgers.app.mock;

import de.adorsys.ledgers.sca.service.AuthCodeGenerator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("develop")
public class MockAuthCodeGenerator implements AuthCodeGenerator {
    private String testAuthCode = System.getProperty("de.adorsys.ledgers.sca.service.AuthCodeGenerator.testAuthCode", "123456");

    @Override
    public String generate() {
        return testAuthCode;
    }
}
