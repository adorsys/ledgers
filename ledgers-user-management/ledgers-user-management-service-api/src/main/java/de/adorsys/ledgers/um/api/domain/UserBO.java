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

import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class UserBO {

    private String id;

    private String login;

    private String email;

    private String pin;

    private List<ScaUserDataBO> scaUserData = new ArrayList<>();

    private List<AccountAccessBO> accountAccesses = new ArrayList<>();

    private Collection<UserRoleBO> userRoles =  new ArrayList<>();

    private String branch;

    public UserBO() {
    }

    public UserBO(@NotNull String login,
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

    public List<ScaUserDataBO> getScaUserData() {
        return scaUserData;
    }

    public void setScaUserData(List<ScaUserDataBO> scaUserData) {
        this.scaUserData = scaUserData;
    }

    public List<AccountAccessBO> getAccountAccesses() {
        return accountAccesses;
    }

    public void setAccountAccesses(List<AccountAccessBO> accountAccesses) {
        this.accountAccesses = accountAccesses;
    }

    public Collection<UserRoleBO> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(Collection<UserRoleBO> userRoles) {
        this.userRoles = userRoles;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
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
                Objects.equals(getBranch(), userBO.getBranch());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getLogin(), getEmail(), getPin(), getScaUserData(), getAccountAccesses(), getUserRoles(), getBranch());
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
                '}';
    }
}
