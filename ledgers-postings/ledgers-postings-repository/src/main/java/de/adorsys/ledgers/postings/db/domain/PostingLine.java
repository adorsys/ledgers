package de.adorsys.ledgers.postings.db.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters.LocalDateTimeConverter;

@Entity
public class PostingLine {
	
	/* The record id */
	@Id
	private String id;
	
	/*The associated ledger account*/
	@ManyToOne(optional=false)
	private LedgerAccount account;
	
	@Column(nullable=false, updatable=false)
	private BigDecimal debitAmount;

	@Column(nullable=false, updatable=false)
	private BigDecimal creditAmount;
	
	/*
	 * This is the json representation of the transaction as posted for the
	 * product module.
	 */
	private String details;

	/*
	 * This is the account delivered by this posting. This field is generally
	 * used to backup information associated with the posting if the 
	 * account referenced is not present in the corresponding ledger.
	 * 
	 */
	private String srcAccount;
	
	/*
	 * The id of the last balanced posting line for this account.
	 */
	private String baseLine;
	
	private String subOprSrcId;

	//================================================================================
	// Denormalization layer. All following fields:
	//   - are duplicated from the post
	//   - never change
	// They allow
	//   - decentralization of record data for close accounting period
	//   - Simple computation of balances
	//
	
	/* The time of recording of this posting. */
	@Column(nullable = false, updatable = false)
	@Convert(converter=LocalDateTimeConverter.class)
	private LocalDateTime recordTime;
	
	/*
	 * The unique identifier of this business operation. 
	 * If tow posting have the same operation id, the one with the youngest
	 * record time
	 */
	@Column(nullable = false, updatable = false)
	private String oprId;
	
    /*
     * The source of the operation. For example, payment order may result into many
     * payments. Each payment will be an operation. The oprSrc field will be used to
     * document original payment id. 
     */
    private String oprSrc;
	
	/*
	 * This is the time from which the posting is effective in this account
	 * statement.
	 */
	@Column(nullable = false, updatable = false)
	@Convert(converter=LocalDateTimeConverter.class)
	private LocalDateTime pstTime;
	
	/*
	 * Some posting are mechanical and do not have an influence on the balance
	 * of an account. Depending on the business logic of the product module,
	 * different types of posting might be defined so that the journal can be
	 * used to document all events associated with an account.
	 * 
	 * For a mechanical posting, the same account and amounts must appear in the
	 * debit and the credit side of the posting. Some account statement will not
	 * display mechanical postings while producing the user statement.
	 */
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, updatable = false)
	private PostingType pstType;
	
	/*
	 * This is the status of the posting. 
	 */
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, updatable = false)
	private PostingStatus pstStatus;
	@Column(nullable = false)
    private String hash;
    
    /*
     * The record time of the discarding posting 
     */
    private LocalDateTime discardedTime;

	public void synchPosting(Posting posting){
		this.recordTime = posting.getRecordTime();
		this.oprId = posting.getOprId();
		this.pstTime = posting.getPstTime();
		this.pstType = posting.getPstType();
		this.pstStatus = posting.getPstStatus();
		this.hash = posting.getHash();
		this.discardedTime = posting.getDiscardedTime();
		this.oprSrc = posting.getOprSrc();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public LedgerAccount getAccount() {
		return account;
	}

	public void setAccount(LedgerAccount account) {
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

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public String getSrcAccount() {
		return srcAccount;
	}

	public void setSrcAccount(String srcAccount) {
		this.srcAccount = srcAccount;
	}

	public String getBaseLine() {
		return baseLine;
	}

	public void setBaseLine(String baseLine) {
		this.baseLine = baseLine;
	}

	public LocalDateTime getRecordTime() {
		return recordTime;
	}

	public String getOprId() {
		return oprId;
	}

	public LocalDateTime getPstTime() {
		return pstTime;
	}

	public PostingType getPstType() {
		return pstType;
	}

	public PostingStatus getPstStatus() {
		return pstStatus;
	}

	public String getHash() {
		return hash;
	}

	public LocalDateTime getDiscardedTime() {
		return discardedTime;
	}

	public String getSubOprSrcId() {
		return subOprSrcId;
	}

	public void setSubOprSrcId(String subOprSrcId) {
		this.subOprSrcId = subOprSrcId;
	}

	public String getOprSrc() {
		return oprSrc;
	}

	public void setOprSrc(String oprSrc) {
		this.oprSrc = oprSrc;
	}
	
}
