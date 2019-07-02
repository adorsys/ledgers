package de.adorsys.ledgers.postings.api.exception;

//TODO unused EXCEPTION to be removed https://git.adorsys.de/adorsys/xs2a/psd2-dynamic-sandbox/issues/195
public class DoubleEntryAccountingException extends Exception {
	private static final long serialVersionUID = -930806408364052824L;

	public DoubleEntryAccountingException(String message) {
		super(message);
	}

}
