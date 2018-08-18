package de.adorsys.ledgers.postings.domain;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@NoArgsConstructor
@ToString(callSuper=true)
public class Ledger extends NamedEntity {

	/*The attached chart of account.*/
	@ManyToOne(optional=false)
	@Getter
	private ChartOfAccount coa;
	
	@Builder
	public Ledger(String id, String name, LocalDateTime created, String user, String desc, ChartOfAccount coa) {
		super(id, name, created, user, desc);
		this.coa = coa;
	}
}
