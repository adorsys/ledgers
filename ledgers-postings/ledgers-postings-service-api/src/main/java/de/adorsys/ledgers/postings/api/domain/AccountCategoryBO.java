package de.adorsys.ledgers.postings.api.domain;

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
public enum AccountCategoryBO {
	RE("Revenue",BalanceSideBO.Cr),
	EX("Expense",BalanceSideBO.Dr),
	AS("Asset",BalanceSideBO.Dr),
	LI("Liability",BalanceSideBO.Cr),
	EQ("Equity",BalanceSideBO.Cr),
	NOOP("Non-Operating Income or Expenses",BalanceSideBO.DrCr),
	NORE("Non-Operating Revenue",BalanceSideBO.Cr),
	NOEX("Non-Operating Expenses",BalanceSideBO.Dr);
	
	private final String desc;
	private final BalanceSideBO defaultBs;
	
	private AccountCategoryBO(String desc, BalanceSideBO bs) {
		this.desc = desc;
		this.defaultBs=bs;
	}
	public String getDesc() {
		return desc;
	}
	public BalanceSideBO getDefaultBs() {
		return defaultBs;
	}
}
