package de.adorsys.ledgers.postings.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Embeddable
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostingLine {
	
	/*The associated ledger account*/
	@Column(nullable=false, updatable=false)
	private String account;
	
	@Column(nullable=false, updatable=false)
	private BigDecimal amount;
	
	@Column(nullable=false, updatable=false)
	private String details;
	
	@Column(nullable=false, updatable=false)
	@Enumerated(EnumType.STRING)
	private PostingSide side;

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
	 * The ledger governing this posting.
	 */
	@ManyToOne(optional=false)
	@Setter
	private Ledger ledger;

	/*
	 * This field is used to secure the timestamp of the ledger opening.
	 * A posting time can not be carry a posting 
	 */
	@Column(nullable = false, updatable = false)
	@Setter
	private LocalDateTime lastClosing;
	
}
