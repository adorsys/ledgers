package de.adorsys.ledgers.postings.db.adapter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.adorsys.ledgers.postings.db.domain.Ledger;
import de.adorsys.ledgers.postings.db.exception.LedgerWithIdNotFoundException;
import de.adorsys.ledgers.postings.db.repository.LedgerRepository;

/**
 * 
 * Adapter providing more choice to access ledger repository.
 * 
 * @author fpo
 *
 */
@Service
public class LedgerRepositoryAdapter {

	@Autowired
	private LedgerRepository ledgerRepository;

	public Ledger loadLedger(String id) throws LedgerWithIdNotFoundException {
		return ledgerRepository.findById(id).orElseThrow(() -> new LedgerWithIdNotFoundException(id));

	}
}
