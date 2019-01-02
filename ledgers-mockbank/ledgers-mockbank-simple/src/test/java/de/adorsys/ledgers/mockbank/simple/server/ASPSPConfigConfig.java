package de.adorsys.ledgers.mockbank.simple.server;

import org.springframework.context.annotation.Bean;

import de.adorsys.ledgers.deposit.api.service.domain.ASPSPConfigSource;

//@Configuration
public class ASPSPConfigConfig{

	@Bean
	public ASPSPConfigSource config() {
		return new MockBankConfigSource();
	}
}
