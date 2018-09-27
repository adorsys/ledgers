package de.adorsys.ledgers.postings.domain;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Getter
@ToString(callSuper=true)
@Table(uniqueConstraints={@UniqueConstraint(columnNames = {"ledger_id", "name"}, name="LedgerAccount_ledger_id_name_unique")})
@NoArgsConstructor
public class LedgerAccount extends NamedEntity {
	
	/*Name of the containing ledger*/
	@ManyToOne(optional=false)
	private Ledger ledger;

	/* The parent of this ledger account*/
	@ManyToOne(optional=true)
	private LedgerAccount parent;
	
	/*Reference to the account type from the ledger's chart of account.*/
	@ManyToOne(optional=false)
	private LedgerAccountType accountType;
	
	/*The detail level of this ledger account*/
	private int level;

	@Builder
	public LedgerAccount(String id, LocalDateTime created, String user, String shortDesc, String longDesc, String name,
			Ledger ledger, LedgerAccount parent, LedgerAccountType accountType,
			int level) {
		super(id, created, user, shortDesc, longDesc, name);
		this.ledger = ledger;
		this.parent = parent;
		this.accountType = accountType;
		this.level = level;
	}
}
