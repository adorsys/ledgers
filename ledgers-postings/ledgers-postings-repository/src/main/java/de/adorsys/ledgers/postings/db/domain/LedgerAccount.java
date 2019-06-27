package de.adorsys.ledgers.postings.db.domain;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * An account is used to group related posting lines.
 * 
 * @author fpo
 *
 */
@Entity
@Table(uniqueConstraints = {
		@UniqueConstraint(columnNames = { "ledger_id", "name" }, name = "LedgerAccount_ledger_id_name_unique") })
public class LedgerAccount extends NamedEntity {

	/* Name of the containing ledger */
	@ManyToOne(optional = false)
	private Ledger ledger;

	/*
	 * Identifier of the parent of this account in the containing chart of
	 * account.
	 * 
	 * Null if there is no parent account.
	 * 
	 */
	@ManyToOne(optional = true)
	private LedgerAccount parent;

	/*
	 * The Chart of account defining this ledger account.
	 * 
	 * This can be inherited from the parent account. Must always
	 * be the same as the parent chart of account if any.
	 */
	@ManyToOne(optional = false)
	private ChartOfAccount coa;
	
	/* 
	 * Indicator on what BS side increases the balance of this account.
	 * 
	 *  Helps decides where to display the position in a balance sheet.
	 * 
	 */
	@Column(nullable = false, updatable = false)
	@Enumerated (EnumType.STRING)
	private BalanceSide balanceSide;
	
	/*
	 * Each account belongs to an account category
	 */
	@Column(nullable = false, updatable = false)
	@Enumerated (EnumType.STRING)
	private AccountCategory category;

	public LedgerAccount(String id, LocalDateTime created, String user, String shortDesc, String longDesc, String name,
                         Ledger ledger, LedgerAccount parent, ChartOfAccount coa, BalanceSide balanceSide,
                         AccountCategory category) {
		super(id, created, user, shortDesc, longDesc, name);
		this.ledger = ledger;
		this.parent = parent;
		this.coa = coa;
		this.balanceSide = balanceSide;
		this.category = category;
	}

	public LedgerAccount() {
		super();
	}

	public Ledger getLedger() {
		return ledger;
	}

	public LedgerAccount getParent() {
		return parent;
	}

	public ChartOfAccount getCoa() {
		return coa;
	}

	public BalanceSide getBalanceSide() {
		return balanceSide;
	}

	public AccountCategory getCategory() {
		return category;
	}

	@Override
	public String toString() {
		return "LedgerAccount [ledger=" + ledger + ", parent=" + parent + ", coa=" + coa + ", balanceSide="
				+ balanceSide + ", category=" + category + "] [super: " + super.toString() + "]";
	}

	public void setLedger(Ledger ledger) {
		this.ledger = ledger;
	}

	public void setParent(LedgerAccount parent) {
		this.parent = parent;
	}

	public void setCoa(ChartOfAccount coa) {
		this.coa = coa;
	}

	public void setBalanceSide(BalanceSide balanceSide) {
		this.balanceSide = balanceSide;
	}

	public void setCategory(AccountCategory category) {
		this.category = category;
	}
}
