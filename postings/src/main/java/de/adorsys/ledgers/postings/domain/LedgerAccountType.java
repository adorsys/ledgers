package de.adorsys.ledgers.postings.domain;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@NoArgsConstructor
@Getter
@ToString(callSuper = true)
@Table(uniqueConstraints = {
		@UniqueConstraint(columnNames = { "coa_id", "name" }, name = "LedgerAccountType_coa_name_unique") })
public class LedgerAccountType extends NamedEntity {

	/* Containing chart of account. */
	/* The attached chart of account. */
	@ManyToOne(optional = false)
	private ChartOfAccount coa;

	/* For the root object, the parent carries the name of the object. */
	@Column(nullable = false)
	private String parent;

	/* The detail level of this ledger account type */
	private int level;

	/* 
	 * Indicator on what BS side increases the balance of this account.
	 * 
	 *  Helps decides where to display the position in a balance sheet.
	 * 
	 */
	@Column(nullable = false, updatable = false)
	@Enumerated (EnumType.STRING)
	private BalanceSide balanceSide;

	/**
	 * All arg constructor for a ledger account type
	 * 
	 * @param id
	 *            the technical identifier
	 * @param created
	 * @param user
	 * @param shortDesc
	 *            The literal string describing this ledger account type
	 * @param longDesc
	 *            The literal string describing this ledger account type
	 * @param name
	 *            the unique logical reference to this object. Made out of a
	 *            local key and the parent name
	 * @param coa
	 *            the reference to the containing chart of account.
	 * @param parent
	 *            The parent key.
	 * @param level
	 * @param increasesTo
	 *            states whether these types of accounts increase on the debit
	 *            or credit side.
	 */
	@Builder
	public LedgerAccountType(String id, LocalDateTime created, String user, String shortDesc, String longDesc,
			String name, ChartOfAccount coa, String parent, int level, BalanceSide balanceSide) {
		super(id, created, user, shortDesc, longDesc, name);
		this.coa = coa;
		this.parent = parent;
		this.level = level;
		this.balanceSide = balanceSide;
	}
}
