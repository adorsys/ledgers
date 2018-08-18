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
@Getter
@ToString(callSuper=true)
public class LedgerAccountType extends NamedEntity {
	
	/*Containing chart of account.*/
	/*The attached chart of account.*/
	@ManyToOne(optional=false)
	private ChartOfAccount coa;

	/*For the root object, the parent carries the name of the object.*/
	@Column(nullable=false)
	private String parent;

	/*The detail level of this ledger account type*/
	private int level;

	@Builder
	public LedgerAccountType(String id, String name, LocalDateTime created, String user, String desc,
			ChartOfAccount coa, String parent, int level) {
		super(id, name, created, user, desc);
		this.coa = coa;
		this.parent = parent;
		this.level = level;
	}

}
