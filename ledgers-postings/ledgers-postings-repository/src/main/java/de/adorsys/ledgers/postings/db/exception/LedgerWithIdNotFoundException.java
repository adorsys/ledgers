package de.adorsys.ledgers.postings.db.exception;

//TODO unused EXCEPTION to be removed https://git.adorsys.de/adorsys/xs2a/psd2-dynamic-sandbox/issues/195
public class LedgerWithIdNotFoundException extends Exception {
	private static final long serialVersionUID = -4332509800720032098L;
	private final String id;
	public LedgerWithIdNotFoundException(String id) {
		super(String.format("Ledger with id %s not found", id));
		this.id = id;
	}
	public String getId() {
		return id;
	}
	
}
