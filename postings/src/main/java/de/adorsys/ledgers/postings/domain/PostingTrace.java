package de.adorsys.ledgers.postings.domain;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;

import com.fasterxml.jackson.core.JsonProcessingException;

import de.adorsys.ledgers.postings.utils.RecordHashHelper;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
@Entity
@Getter
@ToString
@NoArgsConstructor
public class PostingTrace extends HashRecord {
    private static final RecordHashHelper RECORD_HASH_HELPER = new RecordHashHelper();

    @Id
    private String id;

    /*
     * The position of the target posting in the list.
     */
    @Column(nullable = false, updatable = false)
    private int pos;

    /*The source posting id*/
    @Column(nullable = false, updatable = false)
    private String srcPstId;

    /*The hash value of the src posting*/
    @Column(nullable = false, updatable = false)
    private String srcPstHash;

    /*The target posting id. Posting receiving.*/
    @Column(nullable = false, updatable = false)
    private String tgtPstId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime recordTime;

    @Builder
    public PostingTrace(String id, int pos, String srcPstId, String srcPstHash, String tgtPstId, String antTraceId,
                        String antTraceHash) {
        super();
        this.id = id;
        this.pos = pos;
        this.srcPstId = srcPstId;
        this.srcPstHash = srcPstHash;
        this.tgtPstId = tgtPstId;
        this.antecedentId = antTraceId;
        this.antecedentHash = antTraceHash;
    }

    @PrePersist
    public void hash() {
        if (hash != null) {
            throw new IllegalStateException("Can not update a posting trace.");
        }
        recordTime = LocalDateTime.now();
        try {
            hash = RECORD_HASH_HELPER.computeRecHash(this);
        } catch (NoSuchAlgorithmException | JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
