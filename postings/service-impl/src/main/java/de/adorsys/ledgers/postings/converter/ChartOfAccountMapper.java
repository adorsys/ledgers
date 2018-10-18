package de.adorsys.ledgers.postings.converter;

import de.adorsys.ledgers.postings.domain.ChartOfAccount;
import de.adorsys.ledgers.postings.domain.ChartOfAccountBO;
import de.adorsys.ledgers.postings.domain.Ledger;
import de.adorsys.ledgers.postings.domain.LedgerBO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ChartOfAccountMapper {
    ChartOfAccountBO toChartOfAccountBO(ChartOfAccount coa);
    ChartOfAccount toChartOfAccount(ChartOfAccountBO coa);
}
