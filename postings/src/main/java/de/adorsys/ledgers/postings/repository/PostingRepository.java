package de.adorsys.ledgers.postings.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import de.adorsys.ledgers.postings.domain.Posting;

public interface PostingRepository extends CrudRepository<Posting, String> {
	List<Posting> findByOprId(String oprId);

	Optional<Posting> findFirstOptionalByLedgerOrderByRecordTimeDesc(String ledger);
}
