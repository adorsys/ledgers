package de.adorsys.ledgers.middleware.impl.test;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class ObjectMapperConfig {
	
	@Bean
	public ObjectMapper mapper() {
		return new ObjectMapper();
	}
}
