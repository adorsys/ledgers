/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.postings.db.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * The id of a statement is generally identical to the id of the documenting posting.
 *
 * @author fpo
 */
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Entity
public class AccountStmt extends FinancialStmt {

    /*The associated ledger account*/
    @ManyToOne(optional = false)
    private LedgerAccount account;

    /**
     * Identifier of the younges posting by posting time.
     */
    @OneToOne
    private PostingTrace youngestPst;

    /**
     * The debit amount cummulated so far.
     */
    @Column(nullable = false)
    private BigDecimal totalDebit;

    /**
     * The credit amount cummulated so far.
     */
    @Column(nullable = false)
    private BigDecimal totalCredit;

    @PrePersist
    public void prePersist() {
        setId(makeId(account, getPstTime(), getStmtSeqNbr()));
    }

    /**
     * Return the id of the posting being overriding by this posting.
     *
     * @return the id of the posting being overriding by this posting.
     */
    public final Optional<String> clonedId() {
        return Optional.ofNullable(getStmtSeqNbr() <= 0 ? null : makeId(account, getPstTime(), getStmtSeqNbr() - 1));
    }

    public static String makeId(LedgerAccount account, LocalDateTime pstTime, int stmtSeqNbr) {
        return makeOperationId(account, pstTime) + "_" + stmtSeqNbr;
    }

    public static String makeOperationId(LedgerAccount account, LocalDateTime pstTime) {
        return account.getId() + "_" + pstTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

}
