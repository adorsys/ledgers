/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.postings.db.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;

/**
 * The existence or value of a ledger entity is always considered relative to
 * the posting date.
 * 
 * When a book is closed, modification on ledger entities must lead to the
 * creation of new entities.
 * 
 * @author fpo
 *
 */
@Getter
@Setter
@EqualsAndHashCode
@MappedSuperclass
public abstract class NamedEntity extends BaseEntity {
	
	/*Business identifier.  Always unique in a certain scope. Generally in the scope of it's container.*/
	@Column(nullable=false)
	private String name;

	protected NamedEntity(String id, LocalDateTime created, String user, String shortDesc, String longDesc, String name) {
		super(id, created, user, shortDesc, longDesc);
		this.name = name;
	}

	protected NamedEntity() {
		super();
	}

	@Override
	public String toString() {
		return "NamedEntity [name=" + name + "] [super: " + super.toString() + "]";
	}

}
