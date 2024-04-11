/*
 * Copyright (c) 2018-2024 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.postings.api.domain;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * An account is used to group related posting lines.
 *
 * @author fpo
 */
@Data
public class LedgerAccountBO extends NamedBO {

    /* Name of the containing ledger */
    private LedgerBO ledger;

    /**
     * Identifier of the parent of this account in the containing chart of
     * account.
     * <p>
     * Null if there is no parent account.
     */
    private LedgerAccountBO parent;

    /**
     * The Chart of account defining this ledger account.
     * <p>
     * This can be inherited from the parent account. Must always
     * be the same as the parent chart of account if any.
     */
    private ChartOfAccountBO coa;

    /**
     * Indicator on what BS side increases the balance of this account.
     * <p>
     * Helps decides where to display the position in a balance sheet.
     */
    private BalanceSideBO balanceSide;

    /**
     * Each account belongs to an account category
     */
    private AccountCategoryBO category;

    public LedgerAccountBO() {
    }

    public LedgerAccountBO(String name, LedgerBO ledger) {
        super(name);
        this.ledger = ledger;
    }

    public LedgerAccountBO(String name, LedgerAccountBO parent) {
        super(name);
        this.parent = parent;
    }

    public LedgerAccountBO(String name, String id, LocalDateTime created, String userDetails, String shortDesc, String longDesc, LedgerBO ledger, LedgerAccountBO parent, ChartOfAccountBO coa, BalanceSideBO balanceSide, AccountCategoryBO category) {
        super(name, id, created, userDetails, shortDesc, longDesc);
        this.ledger = ledger;
        this.parent = parent;
        this.coa = coa;
        this.balanceSide = balanceSide;
        this.category = category;
    }

}
