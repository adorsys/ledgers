package de.adorsys.ledgers.middleware.api.service;

import java.io.IOException;

import de.adorsys.ledgers.middleware.api.domain.sca.SCAResponseTO;

public interface TokenStorageService {
	
	public SCAResponseTO fromBytes(byte[] tokenBytes) throws IOException;
	
	public byte[] toBytes(SCAResponseTO response) throws IOException;

	public <T extends SCAResponseTO> T fromBytes(byte[] tokenBytes, Class<T> klass) throws IOException;

	public String toBase64String(SCAResponseTO response) throws IOException;
}
