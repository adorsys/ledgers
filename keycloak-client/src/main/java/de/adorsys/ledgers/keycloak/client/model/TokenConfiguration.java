package de.adorsys.ledgers.keycloak.client.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenConfiguration {

    private int tokenLifespanInSeconds;

}
