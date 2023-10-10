/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.postings.db.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * An account is used to group related posting lines.
 * 
 * @author fpo
 *
 */
@Getter
@Setter
@Entity
@EqualsAndHashCode
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

}
