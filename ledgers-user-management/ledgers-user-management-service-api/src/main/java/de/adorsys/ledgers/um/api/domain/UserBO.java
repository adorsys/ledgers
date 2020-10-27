/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.ledgers.um.api.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class UserBO {

    private String id;
    private String login;
    private String email;
    private String pin;
    private List<ScaUserDataBO> scaUserData = new ArrayList<>();
    private List<AccountAccessBO> accountAccesses = new ArrayList<>();
    private Collection<UserRoleBO> userRoles = new ArrayList<>();
    private String branch;
    private boolean blocked;
    private boolean systemBlocked;

    public UserBO(@NotNull String login,
                  @NotNull String email,
                  @NotNull String pin) {
        this.login = login;
        this.email = email;
        this.pin = pin;
    }

    public List<String> getRolesAsString() {
        return this.userRoles.stream()
                       .map(Enum::name)
                       .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserBO)) {
            return false;
        }
        UserBO userBO = (UserBO) o;
        return Objects.equals(getId(), userBO.getId()) &&
                       Objects.equals(getLogin(), userBO.getLogin()) &&
                       Objects.equals(getEmail(), userBO.getEmail()) &&
                       Objects.equals(getPin(), userBO.getPin()) &&
                       Objects.equals(getScaUserData(), userBO.getScaUserData()) &&
                       Objects.equals(getAccountAccesses(), userBO.getAccountAccesses()) &&
                       Objects.equals(getUserRoles(), userBO.getUserRoles()) &&
                       Objects.equals(getBranch(), userBO.getBranch()) &&
                       Objects.equals(isBlocked(), userBO.isBlocked()) &&
                       Objects.equals(isSystemBlocked(), userBO.isSystemBlocked());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getLogin(), getEmail(), getPin(), getScaUserData(), getAccountAccesses(), getUserRoles(), getBranch(), isBlocked(), isSystemBlocked());
    }

    @Override
    public String toString() {
        return "UserBO{" +
                       "id='" + id + '\'' +
                       ", login='" + login + '\'' +
                       ", email='" + email + '\'' +
                       ", pin='" + pin + '\'' +
                       ", scaUserData=" + scaUserData +
                       ", accountAccesses=" + accountAccesses +
                       ", userRoles=" + userRoles +
                       ", branch='" + branch + '\'' +
                       ", blocked='" + blocked + '\'' +
                       ", systemBlocked='" + systemBlocked + '\'' +
                       '}';
    }

    public boolean isEnabled() {
        return !isBlocked() && !isSystemBlocked();
    }

    public boolean hasAccessToAccountWithId(String accountId) {
        return accountAccesses.stream()
                       .anyMatch(a -> accountId.equals(a.getAccountId()));
    }

    public void addNewAccess(AccountAccessBO access) {
        accountAccesses.add(access);
    }

    public void updateExistingAccess(AccountAccessBO access) {
        accountAccesses.stream()
                .filter(a -> a.getAccountId().equals(access.getAccountId())).findFirst()
                .ifPresent(a -> {
                    a.setAccessType(access.getAccessType());
                    a.setScaWeight(access.getScaWeight());
                    a.setAccountId(access.getAccountId());
                });
    }

    public boolean hasSCA() {
        return CollectionUtils.isNotEmpty(this.scaUserData);
    }

    public int resolveWeightForAccount(String accountId) {
        return accountAccesses.stream()
                       .filter(a -> a.getAccountId().equals(accountId)).findFirst()
                       .map(AccountAccessBO::getScaWeight)
                       .orElse(0);
    }

    public int resolveScaWeightByIban(String iban) {
        return accountAccesses.stream()
                       .filter(ac -> StringUtils.equalsIgnoreCase(ac.getIban(), iban))
                       .map(AccountAccessBO::getScaWeight)
                       .min(Comparator.comparingInt(Integer::intValue))
                       .orElse(0);
    }

    public int resolveMinimalWeightForIbanSet(Set<String> uniqueIbans) {
        return accountAccesses.stream()
                       .filter(ac -> uniqueIbans.contains(ac.getIban()))
                       .min(Comparator.comparing(AccountAccessBO::getScaWeight))
                       .map(AccountAccessBO::getScaWeight)
                       .orElse(0);
    }

    public Set<String> getAccountIds() {
        return accountAccesses == null
                       ? new HashSet<>()
                       : accountAccesses.stream()
                                 .map(AccountAccessBO::getAccountId)
                                 .collect(Collectors.toSet());
    }

    public int resolveMinimalWeightForReferences(List<AccountAccessBO> references) {
        return accountAccesses.stream()
                       .filter(a -> containedIn(a, references))
                       .min(Comparator.comparing(AccountAccessBO::getScaWeight))
                       .map(AccountAccessBO::getScaWeight)
                       .orElse(0);
    }

    private boolean containedIn(AccountAccessBO access, List<AccountAccessBO> references) {
        return references.stream()
                       .anyMatch(r -> r.getIban().equals(access.getIban())
                                              && Optional.ofNullable(r.getCurrency())
                                                         .map(c -> c.equals(r.getCurrency()))
                                                         .orElse(true));
    }
}