package de.adorsys.ledgers.postings.domain;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@NoArgsConstructor
@ToString(callSuper=true)
/* The name property of a chart of account must be unique. */
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "name" }, name = "Ledger_name_unique") })
public class Ledger extends NamedEntity {

	/*The attached chart of account.*/
	@ManyToOne(optional=false)
	@Getter
	private ChartOfAccount coa;
	
	/*
	 * This field is used to secure the timestamp of the ledger opening.
	 * A posting time can not be carry a posting 
	 */
	@Column(nullable = false, updatable = false)
	private LocalDateTime lastClosing;

	@Builder
	public Ledger(String id, LocalDateTime created, String user, String shortDesc, String longDesc, String name,
			ChartOfAccount coa) {
		super(id, created, user, shortDesc, longDesc, name);
		this.coa = coa;
	}
	
}
