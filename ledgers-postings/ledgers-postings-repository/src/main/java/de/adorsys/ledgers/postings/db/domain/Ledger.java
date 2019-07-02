package de.adorsys.ledgers.postings.db.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
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
