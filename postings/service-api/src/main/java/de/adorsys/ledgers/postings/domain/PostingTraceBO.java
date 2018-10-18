package de.adorsys.ledgers.postings.domain;

import java.time.LocalDateTime;

/**
 * Posting traces a used to keep references on input posting
 * while making some aggregation of balance calculation.
 * <p>
 * We document statements like balances, balance sheets using posting as well.
 * Since posting never change, any statement produced by this module can be
 * reproduced or checked for integrity.
 * <p>
 * Each trace entry keeps reference of an antecedent posting trace.
 * <p>
 * The hash value of this posting also includes:
 * - The hash value of the input posting
 * - The hash value of the antecedent posting trace.
 * <p>
 * Here we record the posting used and not the operation used.
 *
 * @author fpo
 */
public class PostingTraceBO extends HashRecordBO {

    private String id;

    /*
     * The position of the target posting in the list.
     */
    private int pos;

    /*The source posting id*/
    private String srcPstId;

    /*The hash value of the src posting*/
    private String srcPstHash;

    /*The target posting id. Posting receiving.*/
    private String tgtPstId;

    private LocalDateTime recordTime;
}
