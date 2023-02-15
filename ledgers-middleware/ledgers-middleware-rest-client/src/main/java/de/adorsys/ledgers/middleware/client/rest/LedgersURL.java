/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.client.rest;

@SuppressWarnings("java:S1214")
public interface LedgersURL {
	String LEDGERS_URL="${ledgers.url:http://localhost:${server.port}}";
}
