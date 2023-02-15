/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.postings.db.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Document the state of a ledger at the statement date.
 * 
 * @author fpo
 *
 */
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Entity
public class LedgerStmt extends FinancialStmt {

	/* Name of the containing ledger */
	@ManyToOne(optional = false)
	private Ledger ledger;

	@PrePersist
    public void prePersist() {
        setId(makeId(ledger, getPstTime(), getStmtSeqNbr()));
    }
    
    /**
     * Return the id of the posting being overriding by this posting.
     *  
     * @return the id of the posting being overriding by this posting.
     */
    public final Optional<String> clonedId() {
    	return Optional.ofNullable(getStmtSeqNbr()<=0?null:makeId(ledger, getPstTime(), getStmtSeqNbr()-1));
    }
    
    public static String makeId(Ledger ledger, LocalDateTime pstTime, int stmtSeqNbr) {
        return makeOperationId(ledger, pstTime) + "_" + stmtSeqNbr;
    }	
    public static String makeOperationId(Ledger ledger, LocalDateTime pstTime) {
        return ledger.getId() + "_" +  pstTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
