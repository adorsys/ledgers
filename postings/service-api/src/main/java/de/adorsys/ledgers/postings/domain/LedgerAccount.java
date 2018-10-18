package de.adorsys.ledgers.postings.domain;

/**
 * An account is used to group related posting lines.
 * 
 * @author fpo
 *
 */
public class LedgerAccount extends NamedBO {

	/* Name of the containing ledger */
	private LedgerBO ledger;

	/*
	 * Identifier of the parent of this account in the containing chart of
	 * account.
	 * 
	 * Null if there is no parent account.
	 * 
	 */
	private LedgerAccount parent;

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
}
