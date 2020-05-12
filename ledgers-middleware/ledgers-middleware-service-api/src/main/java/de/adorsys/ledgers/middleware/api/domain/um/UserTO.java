package de.adorsys.ledgers.middleware.api.domain.um;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
    private Collection<UserRoleTO> userRoles = new ArrayList<>();

    private String branch;

    private boolean blocked;
    private boolean systemBlocked;

    public UserTO(String login, String email, String pin) {
        this.login = login;
        this.email = email;
        this.pin = pin;
    }

    @JsonIgnore
    public UserTO updateUserBranch(String newBranch) {
        this.branch = newBranch;
        return this;
    }
}