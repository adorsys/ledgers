package de.adorsys.ledgers.postings.api.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A posting trace document the inclusion of a posting in the creation of a 
 * statement.
 * <p>
 * 
 * For each stmt posting, an operation can only be involved once.
 *
 * @author fpo
 */
public class PostingTraceBO {
    private String id;

    /*The target posting id. Posting receiving.*/
    private String tgtPstId;
    
    private LocalDateTime srcPstTime;
    
    /*The target posting id. Posting receiving.*/
    private String srcPstId;

    /*The source operation id*/
    private String srcOprId;

	/*The associated ledger account*/
	private LedgerAccountBO account;
	
	private BigDecimal debitAmount;

	private BigDecimal creditAmount;
	
	private String srcPstHash;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTgtPstId() {
		return tgtPstId;
	}

	public void setTgtPstId(String tgtPstId) {
		this.tgtPstId = tgtPstId;
	}

	public String getSrcOprId() {
		return srcOprId;
	}

	public void setSrcOprId(String srcOprId) {
		this.srcOprId = srcOprId;
	}

	public LedgerAccountBO getAccount() {
		return account;
	}

	public void setAccount(LedgerAccountBO account) {
		this.account = account;
	}

	public BigDecimal getDebitAmount() {
		return debitAmount;
	}

	public void setDebitAmount(BigDecimal debitAmount) {
		this.debitAmount = debitAmount;
	}

	public BigDecimal getCreditAmount() {
		return creditAmount;
	}

	public void setCreditAmount(BigDecimal creditAmount) {
		this.creditAmount = creditAmount;
	}

	public String getSrcPstHash() {
		return srcPstHash;
	}

	public void setSrcPstHash(String srcPstHash) {
		this.srcPstHash = srcPstHash;
	}

	public LocalDateTime getSrcPstTime() {
		return srcPstTime;
	}

	public void setSrcPstTime(LocalDateTime srcPstTime) {
		this.srcPstTime = srcPstTime;
	}

	public String getSrcPstId() {
		return srcPstId;
	}

	public void setSrcPstId(String srcPstId) {
		this.srcPstId = srcPstId;
	}
	
}
