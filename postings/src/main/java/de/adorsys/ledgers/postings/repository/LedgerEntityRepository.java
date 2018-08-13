package de.adorsys.ledgers.postings.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;

@NoRepositoryBean
public interface LedgerEntityRepository<T> extends PagingAndSortingRepository<T, String> {
	List<T> findByName(String name);
	Optional<T> findFirstOptionalByNameAndValidFromBeforeAndValidToAfterOrderByValidFromDesc(String name, LocalDateTime validFrom, LocalDateTime validTo);
}
