package de.adorsys.ledgers.postings.db.domain;

public enum PostingType {
    /*
     * Describes a business transaction involving different accounts and affecting
     * account balances.
     */
    BUSI_TX,

    /*
     * Describes an adjustment transaction involving different accounts and
     * affecting account balances.
     */
    ADJ_TX,

    /*
     * Documents the balance of a ledger account.
     *
     */
    BAL_STMT,

    PnL_STMT,

    BS_STMT,
    /*Document the closing of a ledger.*/
    LDG_CLSNG;
}
