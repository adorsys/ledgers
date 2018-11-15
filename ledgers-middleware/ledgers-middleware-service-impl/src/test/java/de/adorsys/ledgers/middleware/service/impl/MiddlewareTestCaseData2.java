package de.adorsys.ledgers.middleware.service.impl;

import java.util.ArrayList;
import java.util.List;

import de.adorsys.ledgers.middleware.service.domain.account.DepositAccountTO;
import de.adorsys.ledgers.middleware.service.domain.um.UserTO;

/**
 * Used to map middleware test scenario into yml files.
 * 
 * @author fpo
 *
 *
 */
public class MiddlewareTestCaseData2 {
	
	private List<DepositAccountTO> accounts = new ArrayList<>();
	private List<UserTO> users = new ArrayList<>();
	private List<SinglePaymentTestData> singlePaymentTests = new ArrayList<>(); 
	private List<TransactionTestData> transactions = new ArrayList<>();

	public List<DepositAccountTO> getAccounts() {
		return accounts;
	}
	public void setAccounts(List<DepositAccountTO> accounts) {
		this.accounts = accounts;
	}
	public List<UserTO> getUsers() {
		return users;
	}
	public void setUsers(List<UserTO> users) {
		this.users = users;
	}
	public List<SinglePaymentTestData> getSinglePaymentTests() {
		return singlePaymentTests;
	}
	public void setSinglePaymentTests(List<SinglePaymentTestData> singlePaymentTests) {
		this.singlePaymentTests = singlePaymentTests;
	}
	public List<TransactionTestData> getTransactions() {
		return transactions;
	}
	public void setTransactions(List<TransactionTestData> transactions) {
		this.transactions = transactions;
	}
}
