package de.adorsys.ledgers.postings.db.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;

/**
 * The id of a statement is generally identical to the id of the documenting posting.
 * 
 * @author fpo
 *
 */
@Entity
public class AccountStmt extends FinancialStmt {

	/*The associated ledger account*/
	@ManyToOne(optional=false)
	private LedgerAccount account;

	/*
	 * Identifier of the younges posting by posting time.
	 * 
	 */
	@OneToOne
	private PostingTrace youngestPst;

	/*
	 * The debit amount cummulated so far.
	 * 
	 */
	@Column(nullable=false)
	private BigDecimal totalDebit;

	/*
	 * The credit amount cummulated so far.
	 */
	@Column(nullable=false)
	private BigDecimal totalCredit;
	
	@PrePersist
    public void prePersist() {
        setId(makeId(account, getPstTime(), getStmtSeqNbr()));
    }
    
    /**
     * Return the id of the posting being overriding by this posting.
     *  
     * @return
     */
    public final Optional<String> clonedId() {
    	return Optional.ofNullable(getStmtSeqNbr()<=0?null:makeId(account, getPstTime(), getStmtSeqNbr()-1));
    }
    
    public static String makeId(LedgerAccount account, LocalDateTime pstTime, int stmtSeqNbr) {
        return makeOperationId(account, pstTime) + "_" + stmtSeqNbr;
    }	
    public static String makeOperationId(LedgerAccount account, LocalDateTime pstTime) {
        return account.getId() + "_" +  pstTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }	

	public LedgerAccount getAccount() {
		return account;
	}

	public void setAccount(LedgerAccount account) {
		this.account = account;
	}

	public PostingTrace getYoungestPst() {
		return youngestPst;
	}

	public void setYoungestPst(PostingTrace youngestPst) {
		this.youngestPst = youngestPst;
	}

	public BigDecimal getTotalDebit() {
		return totalDebit;
	}

	public void setTotalDebit(BigDecimal totalDebit) {
		this.totalDebit = totalDebit;
	}

	public BigDecimal getTotalCredit() {
		return totalCredit;
	}

	public void setTotalCredit(BigDecimal totalCredit) {
		this.totalCredit = totalCredit;
	}
	
	/**
	 * Operations used to track the balance of the account. 
	 * @return
	public BalanceSide balanceSide() {
		// Take the balance side of the account if defined.
		if(account!=null && BalanceSide.DrCr!=account.getBalanceSide()) { return account.getBalanceSide();}
		
		if(getDebitAmount().subtract(getCreditAmount()).compareTo(BigDecimal.ZERO)>=0) {
			return BalanceSide.Dr;
		}
		return BalanceSide.Cr;
	}
	public BigDecimal debitBalance() {
		return getDebitAmount().subtract(getCreditAmount());
	}
	public BigDecimal creditBalance() {
		return getCreditAmount().subtract(getDebitAmount());
	}	
	public BigDecimal getBalance(BalanceSide balanceSide) {
		if(balanceSide==BalanceSide.Cr) {
			return creditBalance();
		} else {
			return debitBalance();
		}
	}
	 */
	
}
