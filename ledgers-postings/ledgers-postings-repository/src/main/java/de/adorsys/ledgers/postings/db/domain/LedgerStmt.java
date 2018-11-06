package de.adorsys.ledgers.postings.db.domain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;

/**
 * Document the state of a ledger at the statement date.
 * 
 * @author fpo
 *
 */
@Entity
public class LedgerStmt extends FinancialStmt {

	/* Name of the containing ledger */
	@ManyToOne(optional = false)
	private Ledger ledger;

	@PrePersist
    public void prePersist() {
        setId(makeId(ledger, getPstTime(), getStmtSeqNbr()));
    }
    
    /**
     * Return the id of the posting being overriding by this posting.
     *  
     * @return
     */
    public final Optional<String> clonedId() {
    	return Optional.ofNullable(getStmtSeqNbr()<=0?null:makeId(ledger, getPstTime(), getStmtSeqNbr()-1));
    }
    
    public static String makeId(Ledger ledger, LocalDateTime pstTime, int stmtSeqNbr) {
        return makeOperationId(ledger, pstTime) + "_" + stmtSeqNbr;
    }	
    public static String makeOperationId(Ledger ledger, LocalDateTime pstTime) {
        return ledger.getId() + "_" +  pstTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }	
	
	public Ledger getLedger() {
		return ledger;
	}

	public void setLedger(Ledger ledger) {
		this.ledger = ledger;
	}
}
