package de.adorsys.ledgers.um.impl.service;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class HashMacSecretSource {

    @Value("${user-service.jwt.hs256.secret}")
    private String hmacSecret;
}
