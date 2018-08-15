package de.adorsys.ledgers.postings.domain;

import java.math.BigDecimal;

import javax.persistence.Embeddable;
import javax.persistence.Enumerated;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Embeddable
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostingLine {
	
	/*The associated ledger account*/
	private String account;
	
	private BigDecimal amount;
	
	private String details;
	
	@Enumerated
	private PostingSide side;

	/*
	 * This is the account delivered by this posting. This field is generally
	 * used to backup information associated with the posting if the 
	 * account referenced is not present in the corresponding ledger.
	 * 
	 */
	private String srcAccount;

}
