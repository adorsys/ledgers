package de.adorsys.ledgers.mockbank.simple;

import org.springframework.context.annotation.Bean;

import de.adorsys.ledgers.deposit.api.service.domain.ASPSPConfigSource;

public class MockBankConfig {

    @Bean
    public ASPSPConfigSource configSource() {
        return new MockBankConfigSource();
    }
}
