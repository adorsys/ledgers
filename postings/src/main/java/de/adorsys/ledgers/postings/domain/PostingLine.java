package de.adorsys.ledgers.postings.domain;

import java.math.BigDecimal;

import javax.persistence.Embeddable;
import javax.persistence.Enumerated;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Embeddable
@Getter
@ToString
@AllArgsConstructor
public class PostingLine {
	
	/*The associated ledger account*/
	private String account;
	
	private BigDecimal amount;
	
	private String details;
	
	@Enumerated
	private PostingSide side;

}
