package de.adorsys.ledgers.data.upload.model;

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

    public DataPayload() {
    }

    public DataPayload(List<UserTO> users, List<AccountDetailsTO> accounts, List<AccountBalance> balancesList) {
        this.users = users;
        this.accounts = accounts;
        this.balancesList = balancesList;
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
}
