package de.adorsys.ledgers.middleware.service.impl;

import java.util.ArrayList;
import java.util.List;

import de.adorsys.ledgers.deposit.api.domain.DepositAccountBO;
import de.adorsys.ledgers.um.api.domain.UserBO;

/**
 * Used to map middleware test scenario into yml files.
 * 
 * @author fpo
 *
 *
 */
public class MiddlewareTestCaseData {
	
	private List<DepositAccountBO> accounts = new ArrayList<>();
	private List<UserBO> users = new ArrayList<>();
	private List<SinglePaymentTestData> singlePaymentTests = new ArrayList<>(); 
	private List<TransactionTestData> transactions = new ArrayList<>();

	public List<DepositAccountBO> getAccounts() {
		return accounts;
	}
	public void setAccounts(List<DepositAccountBO> accounts) {
		this.accounts = accounts;
	}
	public List<UserBO> getUsers() {
		return users;
	}
	public void setUsers(List<UserBO> users) {
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
