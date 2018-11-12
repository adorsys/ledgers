package de.adorsys.ledgers.middleware.test;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.adorsys.ledgers.deposit.api.service.domain.ASPSPConfigSource;
import de.adorsys.ledgers.middleware.service.impl.MockBankConfigSource;

@Configuration
public class MockBankConfig {

    @Bean
    public ASPSPConfigSource configSource() {
        return new MockBankConfigSource();
    }
}
