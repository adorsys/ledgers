package de.adorsys.ledgers.middleware.api.domain.um;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
@AllArgsConstructor
public class UserCredentialsTO {
    @NotNull
    private String login;
    @NotNull
    private String pin;
    @NotNull
    private UserRoleTO role;
}
