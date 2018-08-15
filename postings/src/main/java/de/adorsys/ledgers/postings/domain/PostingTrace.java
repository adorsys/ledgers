package de.adorsys.ledgers.postings.domain;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * Posting traces a used to keep references on input posting
 * while making some aggregation of balance calculation.
 * 
 * We document statements like balances, balance sheets using posting as well.
 * Since posting never change, any statement produced by this module can be
 * reproduced or checked for integrity.
 * 
 * Each trace entry keeps reference of the antecedent posting trace.
 * 
 * The hash value of this posting also includes:
 * - The hash value of the input posting
 * - The hash value of the antecedent posting trace.
 * 
 * Here we record the posting used and not the operation used.
 * 
 * @author fpo
 *
 */
@Entity
@Getter
@ToString
@NoArgsConstructor
public class PostingTrace {
	
	@Id
	private String id;
	
	/*
	 * The position of the target posting in the list.
	 */
	private int pos;
	
	/*The source posting id*/
	private String srcPstId;
	
	/*The hash value of the src posting*/
	private String srcPstHash;

	/*The target posting id. Posting receiving.*/
	private String tgtPstId;
	
	/*Id of the antecedent trace.*/
	private String antTraceId;

	/*Hash value of the antecedent trace.*/
	private String antTraceHash;
	
	/*
	 *The hash value of this trace 
	 */
	@Setter
	private String hash;

	@Builder
	public PostingTrace(String id, int pos, String srcPstId, String srcPstHash, String tgtPstId, String antTraceId,
			String antTraceHash) {
		super();
		this.id = id;
		this.pos = pos;
		this.srcPstId = srcPstId;
		this.srcPstHash = srcPstHash;
		this.tgtPstId = tgtPstId;
		this.antTraceId = antTraceId;
		this.antTraceHash = antTraceHash;
	}
}
