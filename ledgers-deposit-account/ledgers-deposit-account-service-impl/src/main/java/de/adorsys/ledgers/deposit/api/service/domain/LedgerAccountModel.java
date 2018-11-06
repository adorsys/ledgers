package de.adorsys.ledgers.deposit.api.service.domain;

import de.adorsys.ledgers.postings.api.domain.AccountCategoryBO;
import de.adorsys.ledgers.postings.api.domain.BalanceSideBO;

public class LedgerAccountModel {
    private String shortDesc;
    private String name;
    private AccountCategoryBO category;
    private BalanceSideBO balanceSide;
    private String parent;
	public String getShortDesc() {
		return shortDesc;
	}
	public void setShortDesc(String shortDesc) {
		this.shortDesc = shortDesc;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public AccountCategoryBO getCategory() {
		return category;
	}
	public void setCategory(AccountCategoryBO category) {
		this.category = category;
	}
	public BalanceSideBO getBalanceSide() {
		return balanceSide;
	}
	public void setBalanceSide(BalanceSideBO balanceSide) {
		this.balanceSide = balanceSide;
	}
	public String getParent() {
		return parent;
	}
	public void setParent(String parent) {
		this.parent = parent;
	}
}
