package de.adorsys.ledgers.postings.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;

import de.adorsys.ledgers.postings.utils.Ids;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
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
	
	@Column(nullable=false, updatable=false)
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
	@Column(nullable = false, updatable = false)
	@Setter
	private LocalDateTime recordTime;
	
	/*
	 * The unique identifier of this business operation. 
	 * If tow posting have the same operation id, the one with the youngest
	 * record time
	 */
	@Column(nullable = false, updatable = false)
	@Setter
	private String oprId;

	@Column(nullable = false, updatable = false)
	private int oprSeqNbr = 0;
	
	/*
	 * This is the time from which the posting is effective in this account
	 * statement.
	 */
	@Column(nullable = false, updatable = false)
	@Setter
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
	@Setter
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
	@Setter
	private Ledger ledger;
	
	@Column(nullable = false, updatable = false)
	private String accName;
	
	private void synchPosting(){
		this.recordTime = this.posting.getRecordTime();
		this.oprId = this.posting.getOprId();
		this.oprSeqNbr = this.posting.getOprSeqNbr();
		this.pstTime = this.posting.getPstTime();
		this.pstType = this.posting.getPstType();
		this.pstStatus = this.posting.getPstStatus();
		this.ledger = this.posting.getLedger();
		this.accName = this.account.getName();
	}

	@PrePersist
	public void prePersist(){
		id = Ids.id();
		synchPosting();
	}
}
