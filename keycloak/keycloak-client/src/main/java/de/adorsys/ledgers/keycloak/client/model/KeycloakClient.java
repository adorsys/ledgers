package de.adorsys.ledgers.keycloak.client.model;

import de.adorsys.ledgers.keycloak.client.config.KeycloakClientConfig;
import de.adorsys.ledgers.middleware.api.domain.Constants;
import lombok.Data;
import org.keycloak.representations.idm.ClientRepresentation;

import java.util.Arrays;
import java.util.List;

@Data
public class KeycloakClient {
    private  String clientId;
    private  String clientSecret;
    private  List<String> redirectUrls;
    private  List<String> scopes;
    private  List<String> webOrigins;
    private  boolean publicClient;
    private   boolean fullScopeAllowed = true;

    public KeycloakClient(KeycloakClientConfig configuration, List<String> redirectUrls) {
        this.clientId = configuration.getExternalClientId();
        this.clientSecret = configuration.isPublicClient() ? null : configuration.getClientSecret();
        this.redirectUrls = redirectUrls;
        this.scopes = Constants.ALL_SCOPES;
        this.webOrigins = Arrays.asList("*", "http://localhost:8080");
        this.publicClient = configuration.isPublicClient();
    }

    public boolean notPresent(List<ClientRepresentation> allClients) {
        return allClients.stream()
                       .map(ClientRepresentation::getName)
                       .noneMatch(clientId::equals);
    }
}
