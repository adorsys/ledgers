package de.adorsys.ledgers.postings.db.domain;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
/* The name property of a chart of account must be unique. */
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "name" }, name = "Ledger_name_unique") })
public class Ledger extends NamedEntity {

	/*The attached chart of account.*/
	@ManyToOne(optional=false)
	private ChartOfAccount coa;
	
	/*
	 * This field is used to secure the timestamp of the ledger opening.
	 * A posting time can not be carry a posting 
	 */
	@Column(nullable = false, updatable = false)
	private LocalDateTime lastClosing;

	public Ledger(String id, LocalDateTime created, String user, String shortDesc, String longDesc, String name,
			ChartOfAccount coa, LocalDateTime lastClosing) {
		super(id, created, user, shortDesc, longDesc, name);
		this.coa = coa;
		this.lastClosing = lastClosing;
	}

	public Ledger() {
	}

	public ChartOfAccount getCoa() {
		return coa;
	}

	public LocalDateTime getLastClosing() {
		return lastClosing;
	}

	public void setLastClosing(LocalDateTime lastClosing) {
		this.lastClosing = lastClosing;
	}


	@Override
	public String toString() {
		return "Ledger [coa=" + coa + ", lastClosing=" + lastClosing + "] [super: " + super.toString() + "]";
	}
	
}
