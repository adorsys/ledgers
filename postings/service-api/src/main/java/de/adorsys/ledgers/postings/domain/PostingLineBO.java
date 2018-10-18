package de.adorsys.ledgers.postings.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PostingLineBO {
	
	/* The record id */
	private String id;

	private PostingBO posting;
	
	/*The associated ledger account*/
	private LedgerAccountBO account;
	
	private BigDecimal debitAmount;

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

	//================================================================================
	// Denormalization layer. All following fields:
	//   - are duplicated from the post
	//   - never change
	// They allow
	//   - decentralization of record data for close accounting period
	//   - Simple computation of balances
	//
	
	/* The time of recording of this posting. */
	private LocalDateTime recordTime;
	
	/*
	 * The unique identifier of this business operation. 
	 * If tow posting have the same operation id, the one with the youngest
	 * record time
	 */
	private String oprId;

	private int oprSeqNbr = 0;
	
	/*
	 * This is the time from which the posting is effective in this account
	 * statement.
	 */
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
	private PostingTypeBO pstType;
	
	/*
	 * This is the status of the posting. 
	 */
	private PostingStatusBO pstStatus = PostingStatusBO.POSTED;

	/*
	 * The ledger governing this posting.
	 */
	private LedgerBO ledger;
	
	private String accName;
	
	/*
	 * The Date use to compute interests. This can be different from the posting
	 * date and can lead to the production of other type of balances.
	 */
	private LocalDateTime valTime;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public PostingBO getPosting() {
		return posting;
	}

	public void setPosting(PostingBO posting) {
		this.posting = posting;
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

	public LocalDateTime getRecordTime() {
		return recordTime;
	}

	public void setRecordTime(LocalDateTime recordTime) {
		this.recordTime = recordTime;
	}

	public String getOprId() {
		return oprId;
	}

	public void setOprId(String oprId) {
		this.oprId = oprId;
	}

	public int getOprSeqNbr() {
		return oprSeqNbr;
	}

	public void setOprSeqNbr(int oprSeqNbr) {
		this.oprSeqNbr = oprSeqNbr;
	}

	public LocalDateTime getPstTime() {
		return pstTime;
	}

	public void setPstTime(LocalDateTime pstTime) {
		this.pstTime = pstTime;
	}

	public PostingTypeBO getPstType() {
		return pstType;
	}

	public void setPstType(PostingTypeBO pstType) {
		this.pstType = pstType;
	}

	public PostingStatusBO getPstStatus() {
		return pstStatus;
	}

	public void setPstStatus(PostingStatusBO pstStatus) {
		this.pstStatus = pstStatus;
	}

	public LedgerBO getLedger() {
		return ledger;
	}

	public void setLedger(LedgerBO ledger) {
		this.ledger = ledger;
	}

	public String getAccName() {
		return accName;
	}

	public void setAccName(String accName) {
		this.accName = accName;
	}

	public LocalDateTime getValTime() {
		return valTime;
	}

	public void setValTime(LocalDateTime valTime) {
		this.valTime = valTime;
	}
}
