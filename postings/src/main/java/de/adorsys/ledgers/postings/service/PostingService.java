package de.adorsys.ledgers.postings.service;

import java.util.List;

import de.adorsys.ledgers.postings.basetypes.OperationId;
import de.adorsys.ledgers.postings.domain.Posting;

public interface PostingService {
	
	/**
	 * Creates a new Posting.
	 * 
	 * - If there is another posting with the same operation id
	 * 	- The new posting can only be stored is the oldest is not part of a closed accounting period.
	 * 	- A posting time can not be older than a closed accounting period. 
	 * 
	 * @param posting
	 * @return
	 */
	public Posting newPosting(Posting posting);
	
	/**
	 * Listing all postings associated with this operation id.
	 * 
	 * @param id
	 * @return
	 */
	public List<Posting> findPostingsByOperationId(OperationId oprId);
}
