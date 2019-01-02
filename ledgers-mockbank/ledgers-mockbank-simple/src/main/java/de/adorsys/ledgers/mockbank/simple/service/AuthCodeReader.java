package de.adorsys.ledgers.mockbank.simple.service;

public interface AuthCodeReader {
	String readAuthCode(String opId, String authorizationId);
}
