package de.adorsys.ledgers.postings.db.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;

import de.adorsys.ledgers.postings.db.domain.LedgerAccount;
import de.adorsys.ledgers.postings.db.domain.PostingLine;

public interface PostingLineRepository extends PagingAndSortingRepository<PostingLine, String> {

	List<PostingLine> findByBaseLineAndPstTimeLessThanEqualAndDiscardedTimeIsNullOrderByRecordTimeDesc(String baseLine, LocalDateTime refTime);

	List<PostingLine> findByAccountAndPstTimeLessThanEqualAndDiscardedTimeIsNullOrderByRecordTimeDesc(LedgerAccount account, LocalDateTime refTime);
}
