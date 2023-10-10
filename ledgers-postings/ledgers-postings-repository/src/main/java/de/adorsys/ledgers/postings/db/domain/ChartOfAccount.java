/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.postings.db.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * All accounts used by a company are defined in a chart of account.
 * 
 * @author fpo
 *
 */
@Entity
@EqualsAndHashCode
/* The name property of a chart of account must be unique. */
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "name" }, name = "ChartOfAccount_name_unique") })
public class ChartOfAccount extends NamedEntity {

	public ChartOfAccount(String id, LocalDateTime created, String user, String shortDesc, String longDesc,
			String name) {
		super(id, created, user, shortDesc, longDesc, name);
	}

	public ChartOfAccount() {
		super();
	}
	
	
}
