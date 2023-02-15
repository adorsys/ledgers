/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.postings.db.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters.LocalDateTimeConverter;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * A financial statement will help draw time lines on ledgers and accounts.
 * <p>
 * A trial balance is also a financial statement on the balance sheet that is
 * not closed.
 * <p>
 * You can continuously modify trial balances by adding posting and recomputing
 * some balances.
 * <p>
 * No changes are allowed in a ledger when the ledger has closed with a
 * financial statement.
 *
 * @author fpo
 */
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@MappedSuperclass
public abstract class FinancialStmt {

    /**
     * The record id
     */
    @Id
    private String id;

    /**
     * The corresponding posting.
     */
    @OneToOne
    private Posting posting;

    /**
     * Documents the time of the posting.
     */
    @Column(nullable = false, updatable = false)
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime pstTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private StmtStatus stmtStatus;

    /**
     * Identifier of the latest processed posting. We use this to
     * perform batch processing. The latest posting process will allways be
     * held here.
     */
    @OneToOne
    private PostingTrace latestPst;

    /**
     * The sequence number of the operation processed by this posting.
     * <p>
     * A single statement can be overridden many times as long as the enclosing
     * ledger is not closed. These overriding happens synchronously. Each single one
     * increasing the sequence number of the former posting.
     */
    @Column(nullable = false, updatable = false)
    private int stmtSeqNbr = 0;
}
