package de.adorsys.ledgers.postings.converter;

import de.adorsys.ledgers.postings.domain.Ledger;
import de.adorsys.ledgers.postings.domain.LedgerBO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LedgerMapper {
    LedgerBO toLedgerBO(Ledger ledger);

    Ledger toLedger(LedgerBO ledger);
}
