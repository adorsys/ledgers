package de.adorsys.ledgers.deposit.api.domain;

import java.util.ArrayList;
import java.util.List;


public class DepositAccountDetailsBO {

    private DepositAccountBO account;

    private List<BalanceBO> balances = new ArrayList<>();
    
    

	public DepositAccountDetailsBO() {
		super();
	}

	public DepositAccountDetailsBO(DepositAccountBO account, List<BalanceBO> balances) {
		super();
		this.account = account;
		this.balances = balances;
	}

	public DepositAccountBO getAccount() {
		return account;
	}

	public void setAccount(DepositAccountBO account) {
		this.account = account;
	}

	public List<BalanceBO> getBalances() {
		return balances;
	}

	public void setBalances(List<BalanceBO> balances) {
		this.balances = balances;
	}

}
