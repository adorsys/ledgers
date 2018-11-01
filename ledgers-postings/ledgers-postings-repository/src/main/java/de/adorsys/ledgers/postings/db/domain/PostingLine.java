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
import javax.persistence.PrePersist;

import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters.LocalDateTimeConverter;

import de.adorsys.ledgers.util.Ids;

@Entity
public class PostingLine {
	
	/* The record id */
	@Id
	private String id;

	@ManyToOne(optional=false)
	private Posting posting;
	
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

	@Column(nullable = false, updatable = false)
	private int oprSeqNbr = 0;
	
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
	private PostingStatus pstStatus = PostingStatus.POSTED;

	/*
	 * The ledger governing this posting.
	 */
	@ManyToOne(optional=false)
	private Ledger ledger;
	
	@Column(nullable = false, updatable = false)
	private String accName;
	
	/*
	 * The Date use to compute interests. This can be different from the posting
	 * date and can lead to the production of other type of balances.
	 */
	@Convert(converter=LocalDateTimeConverter.class)
	private LocalDateTime valTime;
	
	private void synchPosting(){
		this.recordTime = this.posting.getRecordTime();
		this.oprId = this.posting.getOprId();
		this.oprSeqNbr = this.posting.getOprSeqNbr();
		this.pstTime = this.posting.getPstTime();
		this.pstType = this.posting.getPstType();
		this.pstStatus = this.posting.getPstStatus();
		this.ledger = this.posting.getLedger();
		this.accName = this.account.getName();
		this.valTime = this.posting.getValTime();
	}

	@PrePersist
	public void prePersist(){
		id = Ids.id();
		synchPosting();
	}

	public PostingLine() {
	}

	public PostingLine(String id, Posting posting, LedgerAccount account, BigDecimal debitAmount,
			BigDecimal creditAmount, String details, String srcAccount, String baseLine) {
		super();
		this.id = id;
		this.posting = posting;
		this.account = account;
		this.debitAmount = debitAmount;
		this.creditAmount = creditAmount;
		this.details = details;
		this.srcAccount = srcAccount;
		this.baseLine = baseLine;
	}

	public String getId() {
		return id;
	}

	public Posting getPosting() {
		return posting;
	}

	public LedgerAccount getAccount() {
		return account;
	}

	public BigDecimal getDebitAmount() {
		return debitAmount;
	}

	public BigDecimal getCreditAmount() {
		return creditAmount;
	}

	public String getDetails() {
		return details;
	}

	public String getSrcAccount() {
		return srcAccount;
	}

	public LocalDateTime getRecordTime() {
		return recordTime;
	}

	public String getOprId() {
		return oprId;
	}

	public int getOprSeqNbr() {
		return oprSeqNbr;
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

	public Ledger getLedger() {
		return ledger;
	}

	public String getAccName() {
		return accName;
	}

	public LocalDateTime getValTime() {
		return valTime;
	}

	public void setPstTime(LocalDateTime pstTime) {
		this.pstTime = pstTime;
	}

	public void setPstType(PostingType pstType) {
		this.pstType = pstType;
	}

	public void setLedger(Ledger ledger) {
		this.ledger = ledger;
	}

	@Override
	public String toString() {
		return "PostingLine [id=" + id + ", posting=" + posting + ", account=" + account + ", debitAmount="
				+ debitAmount + ", creditAmount=" + creditAmount + ", details=" + details + ", srcAccount=" + srcAccount
				+ ", recordTime=" + recordTime + ", oprId=" + oprId + ", oprSeqNbr=" + oprSeqNbr + ", pstTime="
				+ pstTime + ", pstType=" + pstType + ", pstStatus=" + pstStatus + ", ledger=" + ledger + ", accName="
				+ accName + ", valTime=" + valTime + "]";
	}

	public void setRecordTime(LocalDateTime recordTime) {
		this.recordTime = recordTime;
	}

	public void setOprId(String oprId) {
		this.oprId = oprId;
	}

	public String getBaseLine() {
		return baseLine;
	}
	
	/**
	 * Operations used to track the balance of the account. 
	 * @return
	 */
	public BalanceSide balanceSide() {
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
}
