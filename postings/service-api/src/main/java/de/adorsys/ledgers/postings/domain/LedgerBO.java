package de.adorsys.ledgers.postings.domain;

import java.time.LocalDateTime;

public class LedgerBO extends NamedBO {

	/*The attached chart of account.*/
	private ChartOfAccountBO coa;
	
	/*
	 * This field is used to secure the timestamp of the ledger opening.
	 * A posting time can not be carry a posting 
	 */
	private LocalDateTime lastClosing;

	public ChartOfAccountBO getCoa() {
		return coa;
	}

	public void setCoa(ChartOfAccountBO coa) {
		this.coa = coa;
	}

	public LocalDateTime getLastClosing() {
		return lastClosing;
	}

	public void setLastClosing(LocalDateTime lastClosing) {
		this.lastClosing = lastClosing;
	}
}
