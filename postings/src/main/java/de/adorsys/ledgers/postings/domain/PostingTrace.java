package de.adorsys.ledgers.postings.domain;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * Hold references of source postings in case an operation
 * result from an analysis of pre-existing operations.
 * 
 * Here we record the posting used and not the operation used.
 * 
 * @author fpo
 *
 */
@Entity
@Getter
@ToString
@AllArgsConstructor
public class PostingTrace {
	
	@Id
	private String id;
	
	private String srcPstId;

	private String tgtPstId;

}
