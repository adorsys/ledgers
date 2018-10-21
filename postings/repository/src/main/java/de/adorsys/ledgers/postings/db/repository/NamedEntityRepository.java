package de.adorsys.ledgers.postings.db.repository;

import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface NamedEntityRepository<T> extends BaseEntityRepository<T> {
//	Optional<T> findOptionalByName(String name);
}
