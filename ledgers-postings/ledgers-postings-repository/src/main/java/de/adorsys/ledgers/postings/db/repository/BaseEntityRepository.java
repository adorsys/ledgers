package de.adorsys.ledgers.postings.db.repository;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;

@NoRepositoryBean
public interface BaseEntityRepository<T> extends PagingAndSortingRepository<T, String> {}
