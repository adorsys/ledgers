package de.adorsys.ledgers.postings.api.domain;

/**
 * Document the state of a ledger at the statement date.
 * 
 * @author fpo
 *
 */
public class LedgerStmtBO extends FinancialStmtBO {

	/* Name of the containing ledger */
	private LedgerBO ledger;

	public LedgerBO getLedger() {
		return ledger;
	}

	public void setLedger(LedgerBO ledger) {
		this.ledger = ledger;
	}
}
