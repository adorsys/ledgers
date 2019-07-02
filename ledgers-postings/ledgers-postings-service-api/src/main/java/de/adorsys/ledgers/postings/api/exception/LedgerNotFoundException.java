package de.adorsys.ledgers.postings.api.exception;

//TODO unused EXCEPTION to be removed https://git.adorsys.de/adorsys/xs2a/psd2-dynamic-sandbox/issues/195
public class LedgerNotFoundException extends Exception {
    private static final long serialVersionUID = -1713219984198663520L;

    public LedgerNotFoundException(String id) {
        super(String.format("Ledger with id %s not found.", id));
    }

    public LedgerNotFoundException(String name, String id) {
        super(String.format("Ledger with name %s and id %s not found.", name, id));
    }
}
