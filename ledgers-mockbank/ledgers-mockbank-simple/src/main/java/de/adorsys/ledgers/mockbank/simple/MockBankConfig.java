package de.adorsys.ledgers.mockbank.simple;

import de.adorsys.ledgers.deposit.api.service.domain.ASPSPConfigSource;
import org.springframework.context.annotation.Bean;



public class MockBankConfig {

    @Bean
    public ASPSPConfigSource configSource() {
        return new MockBankConfigSource();
    }
}
