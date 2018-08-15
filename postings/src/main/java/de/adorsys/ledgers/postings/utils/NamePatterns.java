package de.adorsys.ledgers.postings.utils;

import org.springframework.stereotype.Service;

import de.adorsys.ledgers.postings.domain.ChartOfAccount;
import de.adorsys.ledgers.postings.domain.Ledger;
import de.adorsys.ledgers.postings.domain.LedgerAccountType;

/**
 * Construct name pattern by prefixing the ContainerName (Not the parent)
 * 
 * @author fpo
 *
 */
@Service
public class NamePatterns {

	private static final String SEPARATOR = "#";

	public String toAccountName(Ledger ledger, LedgerAccountType lat, String suffix){
		if(suffix==null) suffix=getSuffix(lat.getCoa().getName(), lat.getName());
		return buildObjectName(ledger.getName(), suffix);
	}

	public String toAccountTypeName(ChartOfAccount coa, String suffix){
		return buildObjectName(coa.getName(), suffix);
	}
	
	private String buildObjectName(String containerName, String suffix){
		return containerName + SEPARATOR + suffix;
	}
	
	private String getSuffix(String containerName, String objectName){
		return objectName.substring((containerName + SEPARATOR).length());
	}
}
