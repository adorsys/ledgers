package de.adorsys.ledgers.postings.impl.converter;

import de.adorsys.ledgers.postings.api.domain.LedgerBO;
import de.adorsys.ledgers.postings.db.domain.Ledger;
import org.mapstruct.Mapper;

@Mapper
public interface LedgerMapper {
    LedgerBO toLedgerBO(Ledger ledger);

    Ledger toLedger(LedgerBO ledger);
}
