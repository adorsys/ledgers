package de.adorsys.ledgers.middleware.client.rest;

@SuppressWarnings("java:S1214")
public interface LedgersURL {
	String LEDGERS_URL="${ledgers.url:http://localhost:${server.port}}";
}
