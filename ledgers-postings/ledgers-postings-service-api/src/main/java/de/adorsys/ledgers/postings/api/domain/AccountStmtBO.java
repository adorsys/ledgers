package de.adorsys.ledgers.postings.api.domain;

import java.math.BigDecimal;

/**
 * The id of a statement is generally identical to the id of the documenting posting.
 * 
 * @author fpo
 *
 */
public class AccountStmtBO extends FinancialStmtBO {

	/*The associated ledger account*/
	private LedgerAccountBO account;

	/*
	 * Identifier of the younges posting by posting time.
	 * 
	 */
	private PostingTraceBO youngestPst;

	/*
	 * The debit amount cummulated so far.
	 * 
	 */
	private BigDecimal totalDebit;

	/*
	 * The credit amount cummulated so far.
	 */
	private BigDecimal totalCredit;

	public LedgerAccountBO getAccount() {
		return account;
	}

	public void setAccount(LedgerAccountBO account) {
		this.account = account;
	}

	public PostingTraceBO getYoungestPst() {
		return youngestPst;
	}

	public void setYoungestPst(PostingTraceBO youngestPst) {
		this.youngestPst = youngestPst;
	}

	public BigDecimal getTotalDebit() {
		return totalDebit;
	}

	public void setTotalDebit(BigDecimal totalDebit) {
		this.totalDebit = totalDebit;
	}

	public BigDecimal getTotalCredit() {
		return totalCredit;
	}

	public void setTotalCredit(BigDecimal totalCredit) {
		this.totalCredit = totalCredit;
	}
	
	public BigDecimal debitBalance() {
		return BigDecimal.ZERO.add(amt(totalDebit)).subtract(amt(totalCredit));
	}
	public BigDecimal creditBalance() {
		return BigDecimal.ZERO.add(amt(totalCredit)).subtract(amt(totalDebit));
	}
	
	private static BigDecimal amt(BigDecimal amt) {
		return amt==null
				? BigDecimal.ZERO
				: amt;
	}
}
