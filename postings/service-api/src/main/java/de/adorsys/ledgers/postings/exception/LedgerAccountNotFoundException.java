package de.adorsys.ledgers.postings.exception;

public class LedgerAccountNotFoundException extends Exception {
	private static final long serialVersionUID = -1713219984198663520L;

	public LedgerAccountNotFoundException(String message) {
		super(message);
	}

}
