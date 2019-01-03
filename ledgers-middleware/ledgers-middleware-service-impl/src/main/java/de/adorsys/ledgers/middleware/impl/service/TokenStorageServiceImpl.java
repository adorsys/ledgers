package de.adorsys.ledgers.middleware.impl.service;

import java.io.IOException;
import java.util.Base64;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.adorsys.ledgers.middleware.api.domain.sca.SCAConsentResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.SCALoginResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.SCAPaymentResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.SCAResponseTO;
import de.adorsys.ledgers.middleware.api.service.TokenStorageService;

public class TokenStorageServiceImpl implements TokenStorageService {
	ObjectMapper mapper = new ObjectMapper();
	
	@Override
	public SCAResponseTO fromBytes(byte[] tokenBytes) throws IOException {
		String type = readType(tokenBytes);
		if(SCAConsentResponseTO.class.getSimpleName().equals(type)) {
			return mapper.readValue(tokenBytes, SCAConsentResponseTO.class);
		} else if (SCALoginResponseTO.class.getSimpleName().equals(type)) {
			return mapper.readValue(tokenBytes, SCALoginResponseTO.class);
		} else if (SCAPaymentResponseTO.class.getSimpleName().equals(type)) {
			return mapper.readValue(tokenBytes, SCAPaymentResponseTO.class);
		} else {
			return null;
		}
	}

	@Override
	public byte[] toBytes(SCAResponseTO response) throws IOException {
		return mapper.writeValueAsBytes(response);
	}

	@Override
	public <T extends SCAResponseTO> T fromBytes(byte[] tokenBytes, Class<T> klass) throws IOException {
		String type = readType(tokenBytes);
		if(!klass.getSimpleName().equals(type)) {
			return null;
		}
		return mapper.readValue(tokenBytes, klass);
	}

	private String readType(byte[] tokenBytes) throws IOException {
		JsonNode jsonNode = mapper.readTree(tokenBytes);
		JsonNode objectType = jsonNode.get("objectType");
		if(objectType==null) {
			return null;
		}
		String type = objectType.toString();
		return type;
	}

	@Override
	public String toBase64String(SCAResponseTO response) throws IOException {
		return Base64.getEncoder().encodeToString(toBytes(response));
	}

}
