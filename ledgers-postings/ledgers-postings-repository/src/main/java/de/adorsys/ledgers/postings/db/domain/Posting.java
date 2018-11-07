package de.adorsys.ledgers.postings.db.domain;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import de.adorsys.ledgers.postings.db.utils.RecordHashHelper;
import de.adorsys.ledgers.util.hash.HashGenerationException;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters.LocalDateTimeConverter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"opr_id", "discarding_id"}, name = "Posting_opr_id_discarding_id_unique")})
public class Posting extends HashRecord {
    private static final RecordHashHelper RECORD_HASH_HELPER = new RecordHashHelper();

    /* The record id */
    @Id
    private String id;

    /* The user (technically) recording this posting. */
    @Column(nullable = false, updatable = false)
    private String recordUser;

    /* The time of recording of this posting. */
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime recordTime;

    /*
     * The unique identifier of this business operation. The operation
     * identifier differs from the posting identifier in that it is not unique.
     * The same operation, can be repetitively posted if some conditions change.
     * The operation identifier will always be the same for all the postings of
     * an operation. Only one of them will be effective in the account statement
     * at any given time.
     */
    @Column(nullable = false, updatable = false, name = "opr_id")
    private String oprId;

    /* The time of occurrence of this operation. Set by the consuming module. */
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime oprTime;

    /*
     * The type of operation recorded here. The semantic of this information is
     * determined by the consuming module.
     */
    private String oprType;

    /* Details associated with this operation. */
    private String oprDetails;

    /*
     * The source of the operation. For example, payment order may result into many
     * payments. Each payment will be an operation. The oprSrc field will be used to
     * document original payment id.
     */
    private String oprSrc;

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
    @Convert(converter = LocalDateTimeConverter.class)
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
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime valTime;

    //    todo: add description to this field
    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.ALL}, orphanRemoval = true)
    @JoinColumn(name = "posting_id")
    private List<PostingLine> lines = new ArrayList<>();

    /*
     * The id of the discarded posting. In case this posting discards another posting.
     */
    private String discardedId;

    /*
     * The record time of the discarding posting
     */
    @Convert(converter = LocalDateTimeConverter.class)
    private LocalDateTime discardedTime;
    /*
     * The id of the discaring posting/
     */
    @Column(name = "discarding_id")
    private String discardingId;

    public Posting hash() {
        // Skipp computation if a hash exists. Original value
        // shall not be overriden.
        if (hash != null) {
            return this;
        }
        if (recordTime == null) {
            recordTime = LocalDateTime.now();
        }
        try {
            hash = RECORD_HASH_HELPER.computeRecHash(this);
        } catch (HashGenerationException e) {
            throw new IllegalStateException(e);
        }
        return this;
    }

    public void synchLines() {
        lines.forEach(l -> l.synchPosting(this));
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

    public String getOprSrc() {
        return oprSrc;
    }

    public String getDiscardedId() {
        return discardedId;
    }

    public void setDiscardedId(String discardedId) {
        this.discardedId = discardedId;
    }

    public LocalDateTime getDiscardedTime() {
        return discardedTime;
    }

    public void setDiscardedTime(LocalDateTime discardedTime) {
        this.discardedTime = discardedTime;
    }

    public String getDiscardingId() {
        return discardingId;
    }

    public void setDiscardingId(String discardingId) {
        this.discardingId = discardingId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setRecordUser(String recordUser) {
        this.recordUser = recordUser;
    }

    public void setRecordTime(LocalDateTime recordTime) {
        this.recordTime = recordTime;
    }

    public void setOprId(String oprId) {
        this.oprId = oprId;
    }

    public void setOprTime(LocalDateTime oprTime) {
        this.oprTime = oprTime;
    }

    public void setOprType(String oprType) {
        this.oprType = oprType;
    }

    public void setOprDetails(String oprDetails) {
        this.oprDetails = oprDetails;
    }

    public void setOprSrc(String oprSrc) {
        this.oprSrc = oprSrc;
    }

    public void setPstTime(LocalDateTime pstTime) {
        this.pstTime = pstTime;
    }

    public void setPstType(PostingType pstType) {
        this.pstType = pstType;
    }

    public void setPstStatus(PostingStatus pstStatus) {
        this.pstStatus = pstStatus;
    }

    public void setLedger(Ledger ledger) {
        this.ledger = ledger;
    }

    public void setValTime(LocalDateTime valTime) {
        this.valTime = valTime;
    }

    public void setLines(List<PostingLine> lines) {
        this.lines = lines;
    }

}