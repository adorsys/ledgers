package de.adorsys.ledgers.postings.repository;

import java.util.Optional;

import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface NamedEntityRepository<T> extends BaseEntityRepository<T> {
	Optional<T> findOptionalByName(String name);
}
