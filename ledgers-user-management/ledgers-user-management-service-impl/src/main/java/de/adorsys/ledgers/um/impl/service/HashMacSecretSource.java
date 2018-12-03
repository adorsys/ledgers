package de.adorsys.ledgers.um.impl.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HashMacSecretSource {

    @Value("${user-service.jwt.hs256.secret}")
    private String hmacSecret;

	public String getHmacSecret() {
		return hmacSecret;
	}

	public void setHmacSecret(String hmacSecret) {
		this.hmacSecret = hmacSecret;
	}
    
    
}
