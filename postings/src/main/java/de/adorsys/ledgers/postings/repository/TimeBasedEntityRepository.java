package de.adorsys.ledgers.postings.repository;

import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface TimeBasedEntityRepository<T> extends BaseEntityRepository<T> {
	
}
