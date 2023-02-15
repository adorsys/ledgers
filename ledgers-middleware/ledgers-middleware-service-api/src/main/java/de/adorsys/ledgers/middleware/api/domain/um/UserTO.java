/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.middleware.api.domain.um;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
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

    public UserTO(String login, String email, String pin, UserRoleTO role) {
        this.login = login;
        this.email = email;
        this.pin = pin;
        this.userRoles = Collections.singletonList(role);
    }

    @JsonIgnore
    public UserTO updateUserBranch(String newBranch) {
        this.branch = newBranch;
        return this;
    }

    @JsonIgnore
    public boolean hasAccessToAccountWithIban(String iban) {
        return getIbansFromAccess().contains(iban);
    }

    @JsonIgnore
    public boolean hasAccessToAccountsWithIbans(Collection<String> ibans) {
        return getIbansFromAccess().containsAll(ibans);
    }

    private Set<String> getIbansFromAccess() {
        return accountAccesses.stream()
                       .map(AccountAccessTO::getIban)
                       .collect(Collectors.toSet());
    }

    @JsonIgnore
    public boolean isEnabled() {
        return !blocked && !systemBlocked;
    }

    public boolean hasAccessToAccountWithId(String accountId) {
        return getIdsFromAccess().contains(accountId);
    }

    private Set<String> getIdsFromAccess() {
        return accountAccesses.stream()
                       .map(AccountAccessTO::getAccountId)
                       .collect(Collectors.toSet());
    }
}