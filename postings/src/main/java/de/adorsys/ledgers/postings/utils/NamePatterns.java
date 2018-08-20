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

	public String toAccountName(Ledger ledger, String suffix){
		if(suffix==null) throw new IllegalArgumentException("suffix can not be null");
		if(suffix.trim().length()==0) throw new IllegalArgumentException("suffix can not be empty");
		suffix = suffix.trim();
		checkSuffixName(suffix);
		return buildObjectName(ledger.getName(), suffix);
	}

	public String toAccountTypeName(ChartOfAccount coa, String suffix){
		if(suffix==null) throw new IllegalArgumentException("suffix can not be null");
		if(suffix.trim().length()==0) throw new IllegalArgumentException("suffix can not be empty");
		suffix = suffix.trim();
		checkSuffixName(suffix);
		return buildObjectName(coa.getName(), suffix);
	}
	
	private void checkSuffixName(String suffix) {
		if(suffix.contains(SEPARATOR)) throw new IllegalArgumentException(String.format("Suffix %s can not contain separator char: %s", suffix, SEPARATOR));
	}

	private String buildObjectName(String containerName, String suffix){
		return containerName + SEPARATOR + suffix;
	}
}
