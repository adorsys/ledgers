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
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A posting trace document the inclusion of a posting in the creation of a 
 * statement.
 * <p>
 * 
 * For each stmt posting, an operation can only be involved once.
 *
 * @author fpo
 */
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Entity
@Table(uniqueConstraints = {
		@UniqueConstraint(columnNames = { "tgt_pst_id", "src_opr_id" }, name = "PostingTrace_tgt_pst_id_src_opr_id_unique") })
public class PostingTrace {
    @Id
    private String id;

    /*The target posting id. Posting receiving.*/
    @Column(nullable = false, updatable = false, name="tgt_pst_id")
    private String tgtPstId;
    
    @Convert(converter=LocalDateTimeConverter.class)
    private LocalDateTime srcPstTime;

    /*The target posting id. Posting receiving.*/
    @Column(nullable = false)
    private String srcPstId;

    /*The source operation id*/
    @Column(nullable = false, updatable = false, name="src_opr_id")
    private String srcOprId;

	/*The associated ledger account*/
	@ManyToOne(optional=false)
	private LedgerAccount account;
	
	@Column(nullable=false)
	private BigDecimal debitAmount;

	@Column(nullable=false)
	private BigDecimal creditAmount;
	
	private String srcPstHash;
}
