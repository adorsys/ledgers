package de.adorsys.ledgers.postings.exception;

public class LedgerNotFoundException extends Exception {
	private static final long serialVersionUID = -1713219984198663520L;

	public LedgerNotFoundException(String message) {
		super(message);
	}

}
