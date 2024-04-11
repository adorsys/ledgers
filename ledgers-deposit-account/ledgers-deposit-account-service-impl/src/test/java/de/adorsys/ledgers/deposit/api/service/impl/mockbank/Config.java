/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.deposit.api.service.impl.mockbank;

import java.security.Principal;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.adorsys.ledgers.deposit.api.service.domain.ASPSPConfigSource;

@Configuration
public class Config {
    @Bean
    public ASPSPConfigSource configSource() {
        return new MockBankConfigSource();
    }

	@Bean
	public Principal getPrincipal(){
		return () -> "anonymous";
	}
	
	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper()
				.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
	}
}
