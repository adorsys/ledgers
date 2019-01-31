package de.adorsys.ledgers.middleware.api.domain.um;

import org.jetbrains.annotations.NotNull;

public class UserCredentialsTO {

    @NotNull
    private String login;

    @NotNull
    private String pin;

    @NotNull
    private UserRoleTO role;

    public UserCredentialsTO() {
    }

    public UserCredentialsTO(
            @NotNull String login,
            @NotNull String pin,
            @NotNull UserRoleTO role) {
        this.login = login;
        this.role = role;
        this.pin = pin;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public UserRoleTO getRole() {
        return role;
    }

    public void setRole(UserRoleTO role) {
        this.role = role;
    }
}
