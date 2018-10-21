package de.adorsys.ledgers.postings.db.repository;

import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface TimeBasedEntityRepository<T> extends BaseEntityRepository<T> {
	
}
