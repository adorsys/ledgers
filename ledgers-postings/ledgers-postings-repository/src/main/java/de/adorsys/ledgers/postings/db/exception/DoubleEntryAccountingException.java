package de.adorsys.ledgers.postings.db.exception;

import java.math.BigDecimal;

//TODO unused EXCEPTION to be removed https://git.adorsys.de/adorsys/xs2a/psd2-dynamic-sandbox/issues/195
public class DoubleEntryAccountingException extends Exception {
	public DoubleEntryAccountingException(BigDecimal sumDebit, BigDecimal sumCredit) {
		super(String.format("Debit summs up to %s while credit sums up to %s", sumDebit, sumCredit));
	}
}
