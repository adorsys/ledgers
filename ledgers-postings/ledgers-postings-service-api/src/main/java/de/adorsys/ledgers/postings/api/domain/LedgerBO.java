package de.adorsys.ledgers.postings.api.domain;

public class LedgerBO extends NamedBO {

	/*The attached chart of account.*/
	private ChartOfAccountBO coa;
	
	public ChartOfAccountBO getCoa() {
		return coa;
	}

	public void setCoa(ChartOfAccountBO coa) {
		this.coa = coa;
	}
}
