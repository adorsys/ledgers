package de.adorsys.ledgers.postings.utils;

import de.adorsys.ledgers.postings.basetypes.ChartOfAccountName;
import de.adorsys.ledgers.postings.basetypes.LedgerAccountName;
import de.adorsys.ledgers.postings.basetypes.LedgerAccountTypeName;
import de.adorsys.ledgers.postings.basetypes.LedgerName;

/**
 * Construct name pattern by prefixing the ContainerName (Not the parent)
 * 
 * @author fpo
 *
 */
public class NamePatterns {

	private static final String SEPARATOR = "#";

	public LedgerAccountName toAccountName(ChartOfAccountName coaName, LedgerAccountTypeName latName, LedgerName ledgerName){
		String latSuffix = null;
		String prefix = coaName.getValue() + SEPARATOR;
		if(latName.getValue().startsWith(prefix)) {
			latSuffix = getSuffix(prefix,latName.getValue());
		} else {
			latSuffix = latName.getValue();
		}
		return new LedgerAccountName(buildObjectName(ledgerName.getValue(), latSuffix));
	}

	private String buildObjectName(String containerName, String suffix){
		return containerName + SEPARATOR + suffix;
	}
	
	private String getSuffix(String containerName, String objectName){
		return objectName.substring((containerName + SEPARATOR).length());
	}
}
