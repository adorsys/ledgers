/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.postings.api.domain;

import lombok.Data;

import java.math.BigDecimal;

/**
 * The id of a statement is generally identical to the id of the documenting posting.
 * 
 * @author fpo
 *
 */
@Data
public class AccountStmtBO extends FinancialStmtBO {

	/**The associated ledger account*/
	private LedgerAccountBO account;

	/**
	 * Identifier of the younges posting by posting time.
	 */
	private PostingTraceBO youngestPst;

	/**
	 * The debit amount cummulated so far.
	 * 
	 */
	private BigDecimal totalDebit;

	/**
	 * The credit amount cummulated so far.
	 */
	private BigDecimal totalCredit;

	public BigDecimal debitBalance() {
		return BigDecimal.ZERO.add(readAmt(totalDebit)).subtract(readAmt(totalCredit));
	}
	public BigDecimal creditBalance() {
		return BigDecimal.ZERO.add(readAmt(totalCredit)).subtract(readAmt(totalDebit));
	}
	
	private static BigDecimal readAmt(BigDecimal amt) {
		return amt==null
				? BigDecimal.ZERO
				: amt;
	}
}
