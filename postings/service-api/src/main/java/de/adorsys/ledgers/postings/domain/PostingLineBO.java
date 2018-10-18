package de.adorsys.ledgers.postings.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PostingLineBO {
	
	/* The record id */
	private String id;

	private PostingBO posting;
	
	/*The associated ledger account*/
	private LedgerAccount account;
	
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
}
