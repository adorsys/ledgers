package de.adorsys.ledgers.postings.api.domain;


public enum PostingTypeBO {
	/*Describes a business transaction involving different accounts and affecting account balances.*/
	BUSI_TX,
	/*Describes an adjustment transaction involving different accounts and affecting account balances.*/
	/*We don't know trial balances. To revert an ADJ_TX, we have to create a posting with same id a no lines.*/
	ADJ_TX,
	/*Documents the balance of a ledger account.*/
	BAL_STMT,
	/*Processes a profit and lost statement. Uses posting trace to document associated statement.*/
	PnL_STMT,
//	todo: @fpo copy-paste please provide right description
	/*Processes a profit and lost statement.*/
	BS_STMT,
	/*Document the closing of a ledger.*/
	LDG_CLSNG;
}
