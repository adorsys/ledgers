/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.app.mock;

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
