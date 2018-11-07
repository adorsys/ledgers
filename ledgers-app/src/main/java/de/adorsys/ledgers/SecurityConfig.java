package de.adorsys.ledgers;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.Principal;

@Configuration //TODO replace with real security
public class SecurityConfig {

	@Bean
	public Principal getPrincipal(){
		return () -> "anonymous";
	}
}
