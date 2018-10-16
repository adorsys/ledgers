package de.adorsys.ledgers.tests;

import java.security.Principal;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityConfig {

	@Bean
	public Principal getPrincipal(){
		return () -> "anonymous";
	}
}
