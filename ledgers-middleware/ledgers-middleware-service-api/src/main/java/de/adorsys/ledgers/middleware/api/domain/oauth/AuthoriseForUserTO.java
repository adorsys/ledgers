package de.adorsys.ledgers.middleware.api.domain.oauth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthoriseForUserTO {
    private String login;
    private String pin;
    private String userLogin;
}
