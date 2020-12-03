package de.adorsys.ledgers.postings.db.domain;

/**
 *  * The balance side describes the side of the balance where the account balance 
 * increases.
 * @author fpo
 *
 */
@SuppressWarnings("java:S115")
public enum BalanceSide {
	Dr,/*Indicates that the balance of this account increases in the debit.*/
	Cr,/*Indicates that the balance of this account increases in the credit*/
	DrCr;
}
