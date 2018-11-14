package de.adorsys.ledgers.middleware.service.domain.um;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

public class UserTO {

    private String id;

    @NotNull
    private String login;

    @NotNull
    private String email;

    @NotNull
    private String pin;

    private List<ScaUserDataTO> scaUserData = new ArrayList<>();

    private List<AccountAccessTO> accountAccesses = new ArrayList<>();

    public UserTO() {
    }

    public UserTO(@NotNull String login,
                  @NotNull String email,
                  @NotNull String pin) {
        this.login = login;
        this.email = email;
        this.pin = pin;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public List<ScaUserDataTO> getScaUserData() {
        return scaUserData;
    }

    public void setScaUserData(List<ScaUserDataTO> scaUserData) {
        this.scaUserData = scaUserData;
    }

    public List<AccountAccessTO> getAccountAccesses() {
        return accountAccesses;
    }

    public void setAccountAccesses(List<AccountAccessTO> accountAccesses) {
        this.accountAccesses = accountAccesses;
    }
}
