package de.adorsys.ledgers.middleware.api.service;

import java.io.IOException;

import de.adorsys.ledgers.middleware.api.domain.sca.SCAResponseTO;

public interface TokenStorageService {
	
	SCAResponseTO fromBytes(byte[] tokenBytes) throws IOException;
	
	byte[] toBytes(SCAResponseTO response) throws IOException;

	<T extends SCAResponseTO> T fromBytes(byte[] tokenBytes, Class<T> klass) throws IOException;

	String toBase64String(SCAResponseTO response) throws IOException;
}
