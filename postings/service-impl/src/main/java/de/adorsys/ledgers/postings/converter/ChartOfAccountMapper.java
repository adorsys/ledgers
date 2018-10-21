package de.adorsys.ledgers.postings.converter;

import org.springframework.stereotype.Component;

import de.adorsys.ledgers.postings.db.domain.ChartOfAccount;
import de.adorsys.ledgers.postings.domain.ChartOfAccountBO;
import de.adorsys.ledgers.util.CloneUtils;

@Component
public class ChartOfAccountMapper {
	
    public ChartOfAccountBO toChartOfAccountBO(ChartOfAccount coa) {
    	return CloneUtils.cloneObject(coa, ChartOfAccountBO.class);
    }

    public ChartOfAccount toChartOfAccount(ChartOfAccountBO coa) {
    	return CloneUtils.cloneObject(coa, ChartOfAccount.class);
    }
}
