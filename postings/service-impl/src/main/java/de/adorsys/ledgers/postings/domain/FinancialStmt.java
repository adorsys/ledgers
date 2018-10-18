package de.adorsys.ledgers.postings.domain;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * A financial statement will help draw time lines on ledgers. No changes are allowed 
 * in a ledger when the ledger has closed with a financial statement.
 * 
 * @author fpo
 *
 */
@Entity
@NoArgsConstructor
@ToString(callSuper=true)
@Table(uniqueConstraints={
		@UniqueConstraint(columnNames = {"pstId"}, name="FinancialStmt_pstId_unique")
})
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
	
	@Setter
	private LocalDateTime closed;

	@Builder
	public FinancialStmt(String id, LocalDateTime created, String user, String shortDesc, String longDesc, String name,
			Ledger ledger, LocalDateTime pstTime, String pstId) {
		super(id, created, user, shortDesc, longDesc, name);
		this.ledger = ledger;
		this.pstTime = pstTime;
		this.pstId = pstId;
	}
}
