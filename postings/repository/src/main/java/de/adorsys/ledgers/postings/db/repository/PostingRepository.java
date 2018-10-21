package de.adorsys.ledgers.postings.db.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.PagingAndSortingRepository;

import de.adorsys.ledgers.postings.db.domain.Ledger;
import de.adorsys.ledgers.postings.db.domain.Posting;

public interface PostingRepository extends PagingAndSortingRepository<Posting, String> {
	List<Posting> findByOprId(String oprId);

	Optional<Posting> findFirstOptionalByLedgerOrderByRecordTimeDesc(Ledger ledger);
}
