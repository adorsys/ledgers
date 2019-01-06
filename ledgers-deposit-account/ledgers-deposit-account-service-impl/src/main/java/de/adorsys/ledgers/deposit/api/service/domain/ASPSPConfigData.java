package de.adorsys.ledgers.deposit.api.service.domain;

import java.util.ArrayList;
import java.util.List;

public class ASPSPConfigData {

	private static final ClearingAccount NO_ACCOUNT = new ClearingAccount();
	private String name;
	private String ledger;
	private String coaFile;
	private String depositParentAccount;
	private List<LedgerAccountModel> coaExtensions = new ArrayList<>();
	private List<ClearingAccount> clearingAccounts = new ArrayList<>();
	private String cashAccount;
	
	/*Account number present means config is up to date*/
	private String updateMarkerAccountNbr;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getLedger() {
		return ledger;
	}
	public void setLedger(String ledger) {
		this.ledger = ledger;
	}
	public String getCoaFile() {
		return coaFile;
	}
	public void setCoaFile(String coaFile) {
		this.coaFile = coaFile;
	}
	public List<LedgerAccountModel> getCoaExtensions() {
		return coaExtensions;
	}
	public void setCoaExtensions(List<LedgerAccountModel> coaExtensions) {
		this.coaExtensions = coaExtensions;
	}
	public String getDepositParentAccount() {
		return depositParentAccount;
	}
	public void setDepositParentAccount(String depositParentAccount) {
		this.depositParentAccount = depositParentAccount;
	}
	public List<ClearingAccount> getClearingAccounts() {
		return clearingAccounts;
	}
	public void setClearingAccounts(List<ClearingAccount> clearingAccounts) {
		this.clearingAccounts = clearingAccounts;
	}
	public String getClearingAccount(String paymentProduct) {
		return clearingAccounts.stream().filter(c -> c.getPaymentProduct().equals(paymentProduct)).findFirst().orElse(NO_ACCOUNT).getAccountNbr();
	}
	public String getUpdateMarkerAccountNbr() {
		return updateMarkerAccountNbr;
	}
	public void setUpdateMarkerAccountNbr(String updateMarkerAccountNbr) {
		this.updateMarkerAccountNbr = updateMarkerAccountNbr;
	}
	public String getCashAccount() {
		return cashAccount;
	}
	public void setCashAccount(String cashAccount) {
		this.cashAccount = cashAccount;
	}
}
