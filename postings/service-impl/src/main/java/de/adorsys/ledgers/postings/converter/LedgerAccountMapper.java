package de.adorsys.ledgers.postings.converter;

import org.springframework.stereotype.Component;

import de.adorsys.ledgers.postings.db.domain.LedgerAccount;
import de.adorsys.ledgers.postings.domain.LedgerAccountBO;
import de.adorsys.ledgers.util.CloneUtils;

@Component
public class LedgerAccountMapper {
    public LedgerAccountBO toLedgerAccountBO(LedgerAccount ledgerAccount) {
    	return CloneUtils.cloneObject(ledgerAccount, LedgerAccountBO.class);
    }

    public LedgerAccount toLedgerAccount(LedgerAccountBO ledgerAccount) {
    	return CloneUtils.cloneObject(ledgerAccount, LedgerAccount.class);
    }
}
