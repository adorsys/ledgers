package de.adorsys.ledgers.postings.api.domain;

import lombok.Data;

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
@Data
public class PostingTraceBO {
    private String id;

    /*The target posting id. Posting receiving.*/
    private String tgtPstId;
    
    private LocalDateTime srcPstTime;
    
    /*The target posting id. Posting receiving.*/
    private String srcPstId;

    /*The source operation id*/
    private String srcOprId;

	/*The associated ledger account*/
	private LedgerAccountBO account;
	
	private BigDecimal debitAmount;

	private BigDecimal creditAmount;
	
	private String srcPstHash;
}
