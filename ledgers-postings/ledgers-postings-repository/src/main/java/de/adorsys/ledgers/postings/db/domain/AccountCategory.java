/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.postings.db.domain;

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
	EQ("Equity",BalanceSide.Cr),
	NOOP("Non-Operating Income or Expenses",BalanceSide.DrCr),
	NORE("Non-Operating Revenue",BalanceSide.Cr),
	NOEX("Non-Operating Expenses",BalanceSide.Dr);
	
	private final String desc;
	private final BalanceSide defaultBs;
	
	AccountCategory(String desc, BalanceSide bs) {
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
