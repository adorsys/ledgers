/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.postings.api.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import lombok.Data;

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
@Data
public class PostingBO extends HashRecordBO {

    /**
     * The record id
     * */
    private String id;

    /**
     * The user (technically) recording this posting.
     * */
    private String recordUser;

    /**
     * The time of recording of this posting.
     * */
    private LocalDateTime recordTime;

    /**
     * The unique identifier of this business operation. The operation
     * identifier differs from the posting identifier in that it is not unique.
     * The same operation, can be repetitively posted if some conditions change.
     * The operation identifier will always be the same for all the postings of
     * an operation. Only one of them will be effective in the account statement
     * at any given time.
     */
    private String oprId;

    /**
     * The time of occurrence of this operation. Set by the consuming module.
     * */
    private LocalDateTime oprTime;

    /**
     * The type of operation recorded here. The semantic of this information is
     * determined by the consuming module.
     */
    private String oprType;

    /**
     * Details associated with this operation.
     * */
    private String oprDetails;

    /**
     * The source of the operation. For example, payment order may result into many
     * payments. Each payment will be an operation. The oprSrc field will be used to
     * document original payment id. 
     */
    private String oprSrc;
    
    /**
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
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime pstTime;

    /**
     * Some posting are mechanical and do not have an influence on the balance
     * of an account. Depending on the business logic of the product module,
     * different types of posting might be defined so that the journal can be
     * used to document all events associated with an account.
     *
     * For a mechanical posting, the same account and amounts must appear in the
     * debit and the credit side of the posting. Some account statement will not
     * display mechanical postings while producing the user statement.
     */
    private PostingTypeBO pstType;

    /**
     * This is the status of the posting. Can be used to book
     */
    private PostingStatusBO pstStatus = PostingStatusBO.POSTED;

    /**
     * The ledger governing this posting.
     */
    private LedgerBO ledger;

    /**
     * The Date use to compute interests. This can be different from the posting
     * date and can lead to the production of other type of balances.
     */
    private LocalDateTime valTime;

    private List<PostingLineBO> lines = new ArrayList<>();

    /**
     * The id of the discarded posting. In case this posting discards another posting.
     */
    private String discardedId;
    
    /**
     * The record time of the discarding posting 
     */
    private LocalDateTime discardedTime;

    /**
     * The id of the discarding posting/
     */
    private String discardingId;
}
