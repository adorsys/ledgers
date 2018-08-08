package de.adorsys.ledgers.postings.repository;

import org.springframework.data.repository.CrudRepository;

import de.adorsys.ledgers.postings.domain.OpNote;

public interface OpNoteRepository extends CrudRepository<OpNote, String> {}
