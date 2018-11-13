package de.adorsys.ledgers.postings.db.repository;

import de.adorsys.ledgers.postings.db.domain.LedgerAccount;
import de.adorsys.ledgers.postings.db.domain.PostingLine;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PostingLineRepository extends PagingAndSortingRepository<PostingLine, String> {

    List<PostingLine> findByBaseLineAndPstTimeLessThanEqualAndDiscardedTimeIsNullOrderByRecordTimeDesc(String baseLine, LocalDateTime refTime);

    List<PostingLine> findByAccountAndPstTimeLessThanEqualAndDiscardedTimeIsNullOrderByRecordTimeDesc(LedgerAccount account, LocalDateTime refTime);

    List<PostingLine> findByAccountAndPstTimeGreaterThanAndPstTimeLessThanEqualAndDiscardedTimeIsNullOrderByPstTimeDesc(
            LedgerAccount ledgerAccount, LocalDateTime timeFrom, LocalDateTime timeTo);

    Optional<PostingLine> findFirstByAccountAndOprSrc(LedgerAccount ledgerAccount, String operationSourceId);
}
