package de.adorsys.ledgers.postings.db.domain;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * A financial statement will help draw time lines on ledgers. No changes are allowed 
 * in a ledger when the ledger has closed with a financial statement.
 * 
 * @author fpo
 *
 */
@Entity
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
	
	private LocalDateTime closed;

	public FinancialStmt(String id, LocalDateTime created, String user, String shortDesc, String longDesc, String name,
			Ledger ledger, LocalDateTime pstTime, String pstId) {
		super(id, created, user, shortDesc, longDesc, name);
		this.ledger = ledger;
		this.pstTime = pstTime;
		this.pstId = pstId;
	}

	public FinancialStmt() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Ledger getLedger() {
		return ledger;
	}

	public LocalDateTime getPstTime() {
		return pstTime;
	}

	public String getPstId() {
		return pstId;
	}

	public LocalDateTime getClosed() {
		return closed;
	}

	public void setClosed(LocalDateTime closed) {
		this.closed = closed;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((closed == null) ? 0 : closed.hashCode());
		result = prime * result + ((ledger == null) ? 0 : ledger.hashCode());
		result = prime * result + ((pstId == null) ? 0 : pstId.hashCode());
		result = prime * result + ((pstTime == null) ? 0 : pstTime.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		FinancialStmt other = (FinancialStmt) obj;
		if (closed == null) {
			if (other.closed != null)
				return false;
		} else if (!closed.equals(other.closed))
			return false;
		if (ledger == null) {
			if (other.ledger != null)
				return false;
		} else if (!ledger.equals(other.ledger))
			return false;
		if (pstId == null) {
			if (other.pstId != null)
				return false;
		} else if (!pstId.equals(other.pstId))
			return false;
		if (pstTime == null) {
			if (other.pstTime != null)
				return false;
		} else if (!pstTime.equals(other.pstTime))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FinancialStmt [ledger=" + ledger + ", pstTime=" + pstTime + ", pstId=" + pstId + ", closed=" + closed
				+ "] [super: " + super.toString() + "]";
	}
	
}
