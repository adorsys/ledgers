package de.adorsys.ledgers.postings.api.exception;

//TODO unused EXCEPTION to be removed https://git.adorsys.de/adorsys/xs2a/psd2-dynamic-sandbox/issues/195
public class LedgerAccountNotFoundException extends Exception {
    private static final long serialVersionUID = -1713219984198663520L;

    public LedgerAccountNotFoundException(String id) {
        super(String.format("Ledger Account with id %s not found.", id));
    }
    public LedgerAccountNotFoundException(String name, String id) {
        super(String.format("Ledger Account with name %s and id %s not found.", name, id));
    }
}
