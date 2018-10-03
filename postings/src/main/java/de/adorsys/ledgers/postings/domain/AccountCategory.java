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
	RE("Revenue",BalanceSide.CR),
	EX("Expense",BalanceSide.DR),
	AS("Asset",BalanceSide.DR),
	LI("Liability",BalanceSide.CR),
	EQ("Equity",BalanceSide.CR);
	
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
