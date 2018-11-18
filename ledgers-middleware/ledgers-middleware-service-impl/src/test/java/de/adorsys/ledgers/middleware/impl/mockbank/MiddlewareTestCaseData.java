package de.adorsys.ledgers.middleware.impl.mockbank;

import java.util.ArrayList;
import java.util.List;

import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;

/**
 * Used to map middleware test scenario into yml files.
 * 
 * @author fpo
 *
 *
 */
public class MiddlewareTestCaseData extends BalancesData {
	
	private List<AccountDetailsTO> accounts = new ArrayList<>();
	private List<UserTO> users = new ArrayList<>();
	private List<SinglePaymentsData> singlePayments = new ArrayList<>(); 
	private List<BulkPaymentsData> bulkPayments = new ArrayList<>();
	private List<TransactionTestData> transactions = new ArrayList<>();


	public List<AccountDetailsTO> getAccounts() {
		return accounts;
	}
	public void setAccounts(List<AccountDetailsTO> accounts) {
		this.accounts = accounts;
	}
	public List<UserTO> getUsers() {
		return users;
	}
	public void setUsers(List<UserTO> users) {
		this.users = users;
	}
	public List<TransactionTestData> getTransactions() {
		return transactions;
	}
	public void setTransactions(List<TransactionTestData> transactions) {
		this.transactions = transactions;
	}
	public List<SinglePaymentsData> getSinglePayments() {
		return singlePayments;
	}
	public void setSinglePayments(List<SinglePaymentsData> singlePayments) {
		this.singlePayments = singlePayments;
	}
	public List<BulkPaymentsData> getBulkPayments() {
		return bulkPayments;
	}
	public void setBulkPayments(List<BulkPaymentsData> bulkPayments) {
		this.bulkPayments = bulkPayments;
	}
}
