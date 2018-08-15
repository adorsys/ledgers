package de.adorsys.ledgers.postings.domain;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import lombok.Builder;
import lombok.NoArgsConstructor;

/**
 * A financial statement will help draw time lines on ledgers. No changes are allowed 
 * in a ledger as long as the ledger haben closed with a financial statement.
 * 
 * @author fpo
 *
 */
@Entity
@NoArgsConstructor
public class FinancialStmt extends NamedEntity {

	/*Name of the containing ledger*/
	@ManyToOne(optional=false)
	private Ledger ledger;

	/*Documents the time of the posting.*/
	@Column(nullable = false, updatable = false)
	private LocalDateTime pstTime;

	/*Documents the posting holding additional information.*/
	@Column(nullable = false, updatable = false)
	private String pstId;

	@Builder
	public FinancialStmt(String id, String name, LocalDateTime created, String user, String desc, Ledger ledger,
			LocalDateTime pstTime, String pstId) {
		super(id, name, created, user, desc);
		this.ledger = ledger;
		this.pstTime = pstTime;
		this.pstId = pstId;
	}
}
