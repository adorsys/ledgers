package de.adorsys.ledgers.keycloak.client.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KeycloakUser {
    private String id;
    private String login;
    private String password;
    private Boolean enabled;
    private String firstName;
    private String lastName;
    private String email;
    private Boolean emailVerified;
    private List<String> realmRoles;
}
