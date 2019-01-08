package de.adorsys.ledgers.middleware.client.rest;

public interface LedgersURL {
	String LEDGERS_URL="${ledgers.url:http://localhost:${server.port}}";
}
