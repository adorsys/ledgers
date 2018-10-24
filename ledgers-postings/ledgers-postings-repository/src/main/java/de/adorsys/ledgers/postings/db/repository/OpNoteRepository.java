package de.adorsys.ledgers.postings.db.repository;

import org.springframework.data.repository.CrudRepository;

import de.adorsys.ledgers.postings.db.domain.OpNote;

public interface OpNoteRepository extends CrudRepository<OpNote, String> {}
