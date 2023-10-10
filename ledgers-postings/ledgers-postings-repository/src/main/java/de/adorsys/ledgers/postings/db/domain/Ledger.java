/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.postings.db.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@EqualsAndHashCode
@NoArgsConstructor
/* The name property of a chart of account must be unique. */
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "name" }, name = "Ledger_name_unique") })
public class Ledger extends NamedEntity {

	/*The attached chart of account.*/
	@ManyToOne(optional=false)
	private ChartOfAccount coa;

	public Ledger(String id, LocalDateTime created, String user, String shortDesc, String longDesc, String name,
			ChartOfAccount coa) {
		super(id, created, user, shortDesc, longDesc, name);
		this.coa = coa;
	}

	@Override
	public String toString() {
		return "Ledger [coa=" + coa + "] [super: " + super.toString() + "]";
	}
}
