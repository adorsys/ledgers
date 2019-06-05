package de.adorsys.ledgers.data.upload.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;

import javax.validation.constraints.NotNull;
import java.util.List;

public class DataPayload {
    @NotNull
    private List<UserTO> users;
    @NotNull
    private List<AccountDetailsTO> accounts;
    @NotNull
    private List<AccountBalance> balancesList;

    private boolean generatePayments;

    @JsonIgnore
    private String branch;

    public DataPayload() {
    }

    public DataPayload(List<UserTO> users, List<AccountDetailsTO> accounts, List<AccountBalance> balancesList, boolean generatePayments, String branch) {
        this.users = users;
        this.accounts = accounts;
        this.balancesList = balancesList;
        this.generatePayments = generatePayments;
        this.branch = branch;
    }

    public boolean isGeneratePayments() {
        return generatePayments;
    }

    public void setGeneratePayments(boolean generatePayments) {
        this.generatePayments = generatePayments;
    }

    public List<UserTO> getUsers() {
        return users;
    }

    public void setUsers(List<UserTO> users) {
        this.users = users;
    }

    public List<AccountDetailsTO> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<AccountDetailsTO> accounts) {
        this.accounts = accounts;
    }

    public List<AccountBalance> getBalancesList() {
        return balancesList;
    }

    public void setBalancesList(List<AccountBalance> balancesList) {
        this.balancesList = balancesList;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }
}
