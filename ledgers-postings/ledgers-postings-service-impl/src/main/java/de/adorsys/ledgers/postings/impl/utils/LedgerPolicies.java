package de.adorsys.ledgers.postings.impl.utils;

import de.adorsys.ledgers.postings.db.domain.Ledger;
import de.adorsys.ledgers.postings.db.domain.LedgerAccount;
import de.adorsys.ledgers.postings.db.domain.Posting;

public class LedgerPolicies {

    private final Ledger ledger;

    public LedgerPolicies(Ledger ledger) {
        this.ledger = ledger;
    }

    public void validatePostingTime(Posting posting) {
        if (posting.getPstTime() != null && posting.getPstTime().isAfter(ledger.getLastClosing())) {
            return;
        }
        throw new IllegalArgumentException(String.format("posting time %s is before the last ledger closing %s", posting.getPstTime(), ledger.getLastClosing()));
    }

    public void validateProperAccount(LedgerAccount ledgerAccount) {
        if (ledgerAccount.getLedger() != null && ledgerAccount.getLedger().getId().equals(ledger.getId())) {
            return;
        }
        throw new IllegalArgumentException(String.format("Account %S from ledger %s is not owned by this ledger %s", ledgerAccount.getName(), ledgerAccount.getLedger().getName(), ledger.getName()));
    }
}
