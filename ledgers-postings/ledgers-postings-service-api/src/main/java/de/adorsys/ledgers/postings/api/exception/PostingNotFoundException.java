package de.adorsys.ledgers.postings.api.exception;

import de.adorsys.ledgers.postings.api.domain.PostingBO;

public class PostingNotFoundException extends Exception {
	private static final long serialVersionUID = -1713219984198663520L;

	public PostingNotFoundException(String message) {
		super(message);
	}

	public PostingNotFoundException(PostingBO model) {
		this(String.format("Entity of type %s and id %s not found.", model.getClass().getName(), model.getId()));
	}

	public PostingNotFoundException(String accountId, String oprSrcId) {
		this(String.format("Posting with account id %s  and transaction id %s could not be found", accountId, oprSrcId));
	}
	
	

}
