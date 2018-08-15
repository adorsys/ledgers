package de.adorsys.ledgers.postings.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface TimeBasedEntityRepository<T> extends BaseEntityRepository<T> {
	
	List<T> findByName(String name);
	
	// Selecting the most actual non expired copy of the entity.
	Optional<T> findFirstOptionalByNameAndValidFromBeforeAndValidToAfterOrderByValidFromDesc(String name, LocalDateTime validFrom, LocalDateTime validTo);
	
}
