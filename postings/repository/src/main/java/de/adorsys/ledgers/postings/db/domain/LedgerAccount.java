package de.adorsys.ledgers.postings.db.domain;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

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
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((balanceSide == null) ? 0 : balanceSide.hashCode());
		result = prime * result + ((category == null) ? 0 : category.hashCode());
		result = prime * result + ((coa == null) ? 0 : coa.hashCode());
		result = prime * result + ((ledger == null) ? 0 : ledger.hashCode());
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		LedgerAccount other = (LedgerAccount) obj;
		if (balanceSide != other.balanceSide)
			return false;
		if (category != other.category)
			return false;
		if (coa == null) {
			if (other.coa != null)
				return false;
		} else if (!coa.equals(other.coa))
			return false;
		if (ledger == null) {
			if (other.ledger != null)
				return false;
		} else if (!ledger.equals(other.ledger))
			return false;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "LedgerAccount [ledger=" + ledger + ", parent=" + parent + ", coa=" + coa + ", balanceSide="
				+ balanceSide + ", category=" + category + "] [super: " + super.toString() + "]";
	}
	
}
