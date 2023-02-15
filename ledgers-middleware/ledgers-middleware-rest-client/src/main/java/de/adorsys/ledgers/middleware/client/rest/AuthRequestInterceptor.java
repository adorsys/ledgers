/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.client.rest;

import de.adorsys.ledgers.middleware.rest.utils.Constants;
import feign.RequestInterceptor;
import feign.RequestTemplate;

public class AuthRequestInterceptor implements RequestInterceptor {
	
	private static final String BEARER_CONSTANT = "Bearer ";
	
	private String accessToken;

	@Override
	public void apply(RequestTemplate template) {
		if(accessToken!=null) {
			template.header(Constants.AUTH_HEADER_NAME, BEARER_CONSTANT + accessToken);
		}
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
}
