package de.adorsys.ledgers.deposit.api.service.domain;

import java.util.ArrayList;
import java.util.List;

public class ASPSPConfigData {

	private String name;
	private String ledger;
	private String coaFile;
	private List<LedgerAccountModel> coaExtensions = new ArrayList<>();

	private String clearingAccountTarget2;
	private String clearingAccountSepa;
	private String depositParentAccount;
	
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
	public String getClearingAccountTarget2() {
		return clearingAccountTarget2;
	}
	public void setClearingAccountTarget2(String clearingAccountTarget2) {
		this.clearingAccountTarget2 = clearingAccountTarget2;
	}
	public String getClearingAccountSepa() {
		return clearingAccountSepa;
	}
	public void setClearingAccountSepa(String clearingAccountSepa) {
		this.clearingAccountSepa = clearingAccountSepa;
	}
	public String getDepositParentAccount() {
		return depositParentAccount;
	}
	public void setDepositParentAccount(String depositParentAccount) {
		this.depositParentAccount = depositParentAccount;
	}
}
