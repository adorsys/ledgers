package de.adorsys.ledgers.middleware.api.domain.um;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

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

    private Collection<UserRoleTO> userRoles =  new ArrayList<>();


    private String branch;
    
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

	public Collection<UserRoleTO> getUserRoles() {
		return userRoles;
	}

	public void setUserRoles(Collection<UserRoleTO> userRoles) {
		this.userRoles = userRoles;
	}

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    @Override
    public String toString() {
        return "UserTO{" +
                "id='" + id + '\'' +
                ", login='" + login + '\'' +
                ", email='" + email + '\'' +
                ", pin='" + pin + '\'' +
                ", scaUserData=" + scaUserData +
                ", accountAccesses=" + accountAccesses +
                ", userRoles=" + userRoles +
                ", branch=" + branch +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserTO)) return false;
        UserTO userTO = (UserTO) o;
        return Objects.equals(getId(), userTO.getId()) &&
                Objects.equals(getLogin(), userTO.getLogin()) &&
                Objects.equals(getEmail(), userTO.getEmail()) &&
                Objects.equals(getPin(), userTO.getPin()) &&
                Objects.equals(getScaUserData(), userTO.getScaUserData()) &&
                Objects.equals(getAccountAccesses(), userTO.getAccountAccesses()) &&
                Objects.equals(getUserRoles(), userTO.getUserRoles()) &&
                Objects.equals(getBranch(), userTO.getBranch());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getLogin(), getEmail(), getPin(), getScaUserData(), getAccountAccesses(), getUserRoles(), getBranch());
    }
}
