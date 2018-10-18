package de.adorsys.ledgers.postings.domain;

/**
 * An account is used to group related posting lines.
 * 
 * @author fpo
 *
 */
public class LedgerAccountBO extends NamedBO {

	/* Name of the containing ledger */
	private LedgerBO ledger;

	/*
	 * Identifier of the parent of this account in the containing chart of
	 * account.
	 * 
	 * Null if there is no parent account.
	 * 
	 */
	private LedgerAccountBO parent;

	/*
	 * The Chart of account defining this ledger account.
	 * 
	 * This can be inherited from the parent account. Must always
	 * be the same as the parent chart of account if any.
	 */
	private ChartOfAccountBO coa;
	
	/* 
	 * Indicator on what BS side increases the balance of this account.
	 * 
	 *  Helps decides where to display the position in a balance sheet.
	 * 
	 */
	private BalanceSideBO balanceSide;
	
	/*
	 * Each account belongs to an account category
	 */
	private AccountCategoryBO category;

	public LedgerBO getLedger() {
		return ledger;
	}

	public void setLedger(LedgerBO ledger) {
		this.ledger = ledger;
	}

	public LedgerAccountBO getParent() {
		return parent;
	}

	public void setParent(LedgerAccountBO parent) {
		this.parent = parent;
	}

	public ChartOfAccountBO getCoa() {
		return coa;
	}

	public void setCoa(ChartOfAccountBO coa) {
		this.coa = coa;
	}

	public BalanceSideBO getBalanceSide() {
		return balanceSide;
	}

	public void setBalanceSide(BalanceSideBO balanceSide) {
		this.balanceSide = balanceSide;
	}

	public AccountCategoryBO getCategory() {
		return category;
	}

	public void setCategory(AccountCategoryBO category) {
		this.category = category;
	}
}
