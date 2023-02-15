/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.postings.db.domain;

@SuppressWarnings("java:S115")
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
    LDG_CLSNG
}
