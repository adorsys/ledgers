/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.postings.db.repository;

import de.adorsys.ledgers.postings.db.domain.LedgerAccount;
import de.adorsys.ledgers.postings.db.domain.PostingLine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PostingLineRepository extends PagingAndSortingRepository<PostingLine, String> {

    List<PostingLine> findByBaseLineAndPstTimeLessThanEqualAndDiscardedTimeIsNullOrderByRecordTimeDesc(String baseLine, LocalDateTime refTime);

    List<PostingLine> findByAccountAndPstTimeLessThanEqualAndDiscardedTimeIsNullOrderByRecordTimeDesc(LedgerAccount account, LocalDateTime refTime);

    @Query("select pl from PostingLine pl join fetch pl.details " +
                   "where pl.account = :account and pl.pstTime > :fromDt and pl.pstTime <= :toDt and pl.discardedTime is null " +
                   "order by pl.pstTime desc")
    List<PostingLine> findByAccountAndPstTimeGreaterThanAndPstTimeLessThanEqualAndDiscardedTimeIsNullOrderByPstTimeDesc(@Param("account") LedgerAccount ledgerAccount, @Param("fromDt") LocalDateTime timeFrom, @Param("toDt") LocalDateTime timeTo);

    @Query("select pl from PostingLine pl join pl.details " +
                   "where pl.account = :account and pl.pstTime > :fromDt and pl.pstTime <= :toDt and pl.discardedTime is null " +
                   "order by pl.pstTime desc")
    Page<PostingLine> findPostingsByAccountAndDates(@Param("account") LedgerAccount ledgerAccount, @Param("fromDt") LocalDateTime timeFrom, @Param("toDt") LocalDateTime timeTo, Pageable pageable);

    Optional<PostingLine> findFirstByIdAndAccount(String transactionId, LedgerAccount ledgerAccount);
}
