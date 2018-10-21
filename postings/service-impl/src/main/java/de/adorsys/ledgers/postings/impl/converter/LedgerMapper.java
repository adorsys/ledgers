package de.adorsys.ledgers.postings.impl.converter;

import org.springframework.stereotype.Component;

import de.adorsys.ledgers.postings.api.domain.LedgerBO;
import de.adorsys.ledgers.postings.db.domain.Ledger;
import de.adorsys.ledgers.util.CloneUtils;

@Component
public class LedgerMapper {
    public LedgerBO toLedgerBO(Ledger ledger){
    	return CloneUtils.cloneObject(ledger, LedgerBO.class);
    }

    public Ledger toLedger(LedgerBO ledger) {
    	return CloneUtils.cloneObject(ledger, Ledger.class);
    }
}
