package de.adorsys.ledgers.deposit.test;

import java.security.Principal;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityConfig {

	@Bean
	public Principal getPrincipal(){
		return new Principal() {
			
			@Override
			public String getName() {
				return "anonymous";
			}
		};
	}
}
