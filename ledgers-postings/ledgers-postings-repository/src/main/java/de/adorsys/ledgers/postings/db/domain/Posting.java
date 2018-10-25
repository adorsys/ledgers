package de.adorsys.ledgers.postings.db.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;

import de.adorsys.ledgers.postings.db.utils.RecordHashHelper;
import de.adorsys.ledgers.util.hash.HashGenerationException;

/**
 * The word posting is associated with the moment at which the recorded
 * operation is effective in the ledger. Therefore, we distinguish between
 * following moments:
 * - The operation time: the moment at which this operation took place..
 * - The posting time: the moment at which this operation is
 * effectively posted to the ledger (e.g. having influence of an account balance).
 * - The recording time: the moment at which this operation is recorded in the
 * journal.
 */
@Entity
@JsonPropertyOrder(alphabetic = true)
public class Posting extends HashRecord {
    private static final RecordHashHelper RECORD_HASH_HELPER = new RecordHashHelper();

    /* The record id */
    @Id
    private String id;

    /* The user (technically) recording this posting. */
    @Column(nullable = false, updatable = false)
    private String recordUser;

    /* The time of recording of this posting. */
    private LocalDateTime recordTime;

    /*
     * The unique identifier of this business operation. The operation
     * identifier differs from the posting identifier in that it is not unique.
     * The same operation, can be repetitively posted if some conditions change.
     * The operation identifier will always be the same for all the postings of
     * an operation. Only one of them will be effective in the account statement
     * at any given time.
     */
    @Column(nullable = false, updatable = false)
    private String oprId;

    /*
     * The sequence number of the operation processed by this posting.
     *
     * A single operation can be overridden many times as long as the enclosing
     * ledger is not closed. These overriding happens
     * synchronously. Each single one increasing the sequence number of the
     * former posting.
     *
     * This is, the posting id is always a concatenation between the operation
     * id and the sequence number.
     *
     */
    @Column(nullable = false, updatable = false)
    private int oprSeqNbr = 0;

    /* The time of occurrence of this operation. Set by the consuming module. */
    private LocalDateTime oprTime;

    /*
     * The type of operation recorded here. The semantic of this information is
     * determined by the consuming module.
     */
    private String oprType;

    /* Details associated with this operation. */
    private String oprDetails;

    /*
     * This is the time from which the posting is effective in the account
     * statement. This also differs from the recording time in that the posting
     * time can be before or after the recording time.
     *
     * If the posting time if before the recording time, it might have an effect
     * on former postings like past balances. This might lead to the generation
     * of new postings.
     *
     * The posting time of an adjustment operation at day closing is always the
     * last second of that day. So event if that operation is posted while still
     * inside the day, the day closing will be the same. This is, the last
     * second of that day. In the case of an adjustment operation, the posting
     * time and the operation time are identical.
     */
    @Column(nullable = false, updatable = false)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime pstTime;

    /*
     * Some posting are mechanical and do not have an influence on the balance
     * of an account. Depending on the business logic of the product module,
     * different types of posting might be defined so that the journal can be
     * used to document all events associated with an account.
     *
     * For a mechanical posting, the same account and amounts must appear in the
     * debit and the credit side of the posting. Some account statement will not
     * display mechanical postings while producing the user statement.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private PostingType pstType;

    /*
     * This is the status of the posting. Can be used to book
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private PostingStatus pstStatus = PostingStatus.POSTED;

    /*
     * The ledger governing this posting.
     */
    @ManyToOne(optional = false)
    private Ledger ledger;

    /*
     * The Date use to compute interests. This can be different from the posting
     * date and can lead to the production of other type of balances.
     */
    private LocalDateTime valTime;

//    todo: add description to this field
    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.REFRESH, CascadeType.REMOVE})
    private List<PostingLine> lines = new ArrayList<>();

    public Posting() {
    	super();
    }

    public Posting(String recordUser, String recordAntecedentId, String recordAntecedentHash, String oprId,
                   int oprSeqNbr, LocalDateTime oprTime, String oprType, String oprDetails, LocalDateTime pstTime,
                   PostingType pstType, PostingStatus pstStatus, Ledger ledger, LocalDateTime valTime,
                   List<PostingLine> lines) {
        this.recordUser = recordUser;
        this.antecedentId = recordAntecedentId;
        this.antecedentHash = recordAntecedentHash;
        this.oprId = oprId;
        this.oprSeqNbr = oprSeqNbr;
        this.oprTime = oprTime;
        this.oprType = oprType;
        this.oprDetails = oprDetails;
        this.pstTime = pstTime;
        this.pstType = pstType;
        this.ledger = ledger;
        this.valTime = valTime;
        this.lines = lines != null ? lines : new ArrayList<>();
        this.pstStatus = pstStatus != null ? pstStatus : PostingStatus.POSTED;
    }

    public Posting hash() {
        if (hash != null) {
            throw new IllegalStateException("Can not update a posting.");
        }
        recordTime = LocalDateTime.now();
        try {
            hash = RECORD_HASH_HELPER.computeRecHash(this);
        } catch (HashGenerationException e) {
            throw new IllegalStateException(e);
        }
        return this;
    }

    @PrePersist
    public void prePersist() {
        id = oprId + "_" + oprSeqNbr;
    }

	public String getId() {
		return id;
	}

	public String getRecordUser() {
		return recordUser;
	}

	public LocalDateTime getRecordTime() {
		return recordTime;
	}

	public String getOprId() {
		return oprId;
	}

	public int getOprSeqNbr() {
		return oprSeqNbr;
	}

	public LocalDateTime getOprTime() {
		return oprTime;
	}

	public String getOprType() {
		return oprType;
	}

	public String getOprDetails() {
		return oprDetails;
	}

	public LocalDateTime getPstTime() {
		return pstTime;
	}

	public PostingType getPstType() {
		return pstType;
	}

	public PostingStatus getPstStatus() {
		return pstStatus;
	}

	public Ledger getLedger() {
		return ledger;
	}

	public LocalDateTime getValTime() {
		return valTime;
	}

	public List<PostingLine> getLines() {
		return lines;
	}
}