package de.adorsys.ledgers.postings.db.exception;

import java.math.BigDecimal;

public class DoubleEntryAccountingException extends Exception {
	public DoubleEntryAccountingException(BigDecimal sumDebit, BigDecimal sumCredit) {
		super(String.format("Debit summs up to %s while credit sums up to %s", sumDebit, sumCredit));
	}
}
