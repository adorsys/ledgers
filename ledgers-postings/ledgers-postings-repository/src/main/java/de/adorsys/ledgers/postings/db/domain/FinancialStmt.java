package de.adorsys.ledgers.postings.db.domain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;

/**
 * A financial statement will help draw time lines on ledgers and accounts.
 * 
 * A trial balance is also a financial statement on the balance sheet that is
 * not closed.
 * 
 * You can continuously modify trial balances by adding posting and recomputing
 * some balances.
 * 
 * No changes are allowed in a ledger when the ledger has closed with a
 * financial statement.
 * 
 * @author fpo
 *
 */
@Entity
public class FinancialStmt {

    /* The record id */
    @Id
    private String id;
	
	/* Name of the containing ledger */
	@ManyToOne(optional = false)
	private Ledger ledger;

	/* Documents the time of the posting. */
	@Column(nullable = false, updatable = false)
	private LocalDateTime pstTime;

	/* Documents the posting holding additional information. */
	@OneToOne(optional=false)
	private Posting posting;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, updatable = false)
	private StmtStatus stmtStatus;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, updatable = false)
	private StmtType stmtType;
	
	/*
	 * Name of the target object. This can be the account or the ledger itself.
	 * 
	 */
	private String stmtTarget;

	/*
	 * The sequence number of the operation processed by this posting.
	 *
	 * A single statement can be overridden many times as long as the enclosing
	 * ledger is not closed. These overriding happens synchronously. Each single one
	 * increasing the sequence number of the former posting.
	 *
	 */
	@Column(nullable = false, updatable = false)
	private int stmtSeqNbr = 0;

	public FinancialStmt() {
	}

    public FinancialStmt(Ledger ledger, LocalDateTime pstTime, Posting posting, StmtStatus stmtStatus, StmtType stmtType,
			String stmtTarget, int stmtSeqNbr) {
		this.ledger = ledger;
		this.pstTime = pstTime;
		this.posting = posting;
		this.stmtStatus = stmtStatus;
		this.stmtType = stmtType;
		this.stmtTarget = stmtTarget;
		this.stmtSeqNbr = stmtSeqNbr;
	}

	@PrePersist
    public void prePersist() {
        id = makeId(stmtType, stmtTarget, pstTime, stmtSeqNbr);
    }
    
    /**
     * Return the id of the posting being overriding by this posting.
     *  
     * @return
     */
    public final Optional<String> clonedId() {
    	return Optional.ofNullable(stmtSeqNbr<=0?null:makeId(stmtType, stmtTarget, pstTime, stmtSeqNbr-1));
    }
    
    public static String makeId(StmtType stmtType, String stmtTarget, LocalDateTime pstTime, int stmtSeqNbr) {
        return stmtType.name() + "_" + stmtTarget + "_" +  pstTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "_" + stmtSeqNbr;
    }
	
	public Ledger getLedger() {
		return ledger;
	}

	public LocalDateTime getPstTime() {
		return pstTime;
	}

	public int getStmtSeqNbr() {
		return stmtSeqNbr;
	}

	public String getId() {
		return id;
	}

	public Posting getPosting() {
		return posting;
	}

	public StmtType getStmtType() {
		return stmtType;
	}

	public String getStmtTarget() {
		return stmtTarget;
	}

	public StmtStatus getStmtStatus() {
		return stmtStatus;
	}
	
}
