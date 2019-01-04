package de.adorsys.ledgers.mockbank.simple.data.test;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.adorsys.ledgers.middleware.client.rest.AuthRequestInterceptor;

@Configuration
public class AuthConfig {
	@Bean
	public AuthRequestInterceptor getClientAuth() {
		return new AuthRequestInterceptor();
	}
}
