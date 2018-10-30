package de.adorsys.ledgers.postings.api.exception;

public class DoubleEntryAccountingException extends Exception {
	private static final long serialVersionUID = -930806408364052824L;

	public DoubleEntryAccountingException(String message) {
		super(message);
	}

}
