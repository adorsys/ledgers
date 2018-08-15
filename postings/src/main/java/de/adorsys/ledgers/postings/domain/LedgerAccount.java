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
@Getter
@ToString(callSuper=true)
@Table(uniqueConstraints={@UniqueConstraint(columnNames = {"name", "validFrom"}, name="LedgerAccount_name_validFrom_unique")})
@NoArgsConstructor
public class LedgerAccount extends TimeBasedEntity {
	
	/*Name of the containing ledger*/
	@ManyToOne(optional=false)
	private Ledger ledger;

	/*Name of the parent of this account in the containing ledger. */
	/*For the root object, the parent carries the name of the object.*/
	@Column(nullable=false)
	private String parent;
	
	/*Reference to the account type from the ledger's chart of account.*/
	@ManyToOne(optional=false)
	private LedgerAccountType accountType;
	
	/*The detail level of this ledger account*/
	private int level;

	@Builder
	public LedgerAccount(String id, String name, LocalDateTime created, String user, String desc,
			LocalDateTime validFrom, Ledger ledger, String parent, LedgerAccountType accountType, int level) {
		super(id, name, created, user, desc, validFrom);
		this.ledger = ledger;
		this.parent = parent;
		this.accountType = accountType;
		this.level = level;
	}
}
