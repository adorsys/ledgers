package de.adorsys.ledgers.postings.domain;

/**
 * Each account belongs to an account category. We distinguish among following account categories:
 * - Revenue
 * - Expense
 * - Asset
 * - Liability
 * - Equity
 * 
 * @author fpo
 *
 */
public enum AccountCategory {
	RE("Revenue",BalanceSide.Cr),
	EX("Expense",BalanceSide.Dr),
	AS("Asset",BalanceSide.Dr),
	LI("Liability",BalanceSide.Cr),
	EQ("Equity",BalanceSide.Cr);
	
	private final String desc;
	private final BalanceSide defaultBs;
	
	private AccountCategory(String desc, BalanceSide bs) {
		this.desc = desc;
		this.defaultBs=bs;
	}
	public String getDesc() {
		return desc;
	}
	public BalanceSide getDefaultBs() {
		return defaultBs;
	}
}
