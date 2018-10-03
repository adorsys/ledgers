package de.adorsys.ledgers.postings.service.impl;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;

import de.adorsys.ledgers.postings.domain.BaseEntity;
import de.adorsys.ledgers.postings.domain.ChartOfAccount;
import de.adorsys.ledgers.postings.domain.Ledger;
import de.adorsys.ledgers.postings.domain.LedgerAccount;
import de.adorsys.ledgers.postings.domain.NamedEntity;
import de.adorsys.ledgers.postings.exception.NotFoundException;
import de.adorsys.ledgers.postings.repository.ChartOfAccountRepository;
import de.adorsys.ledgers.postings.repository.LedgerAccountRepository;
import de.adorsys.ledgers.postings.repository.LedgerRepository;
import de.adorsys.ledgers.postings.repository.PostingLineRepository;
import de.adorsys.ledgers.postings.repository.PostingRepository;

public class AbstractServiceImpl {
	@Autowired
	protected PostingRepository postingRepository;

	@Autowired
	protected LedgerAccountRepository ledgerAccountRepository;

	@Autowired
	protected ChartOfAccountRepository chartOfAccountRepo;

	@Autowired
	protected Principal principal;

	@Autowired
	protected LedgerRepository ledgerRepository;
	
	@Autowired
	protected PostingLineRepository postingLineRepository;

	protected Ledger loadLedger(Ledger model) throws NotFoundException {
		if (model == null)
			throw nullInfo();

		if (model.getId() != null)
			return ledgerRepository.findById(model.getId()).orElseThrow(() -> notFoundById(model));

		if (model.getName() != null)
			return ledgerRepository.findOptionalByName(model.getName())
					.orElseThrow(() -> notFoundByNameAndContainer(model, null));

		throw insufficientInfo(model);
	}

	protected ChartOfAccount loadCoa(ChartOfAccount model) throws NotFoundException {
		if (model == null)
			throw nullInfo();

		if (model.getId() != null)
			return chartOfAccountRepo.findById(model.getId()).orElseThrow(() -> notFoundById(model));

		if (model.getName() != null)
			return chartOfAccountRepo.findOptionalByName(model.getName())
					.orElseThrow(() -> notFoundByNameAndContainer(model, null));

		throw insufficientInfo(model);
	}

	/*
	 * Load the ledger account. Using the following logic in the given order. 1-
	 * If the Id is provided, we use find by id. 2- If the ledger and the name
	 * is provided, we use them to load the account.
	 */
	protected LedgerAccount loadLedgerAccount(LedgerAccount model) throws NotFoundException {
		if (model == null)
			throw nullInfo();

		if (model.getId() != null) 
			return ledgerAccountRepository.findById(model.getId())
					.orElseThrow(() -> notFoundById(model));
		
		if (model.getLedger() != null && model.getName() != null) {
			Ledger loadedLedger = loadLedger(model.getLedger());
			return ledgerAccountRepository.findOptionalByLedgerAndName(loadedLedger, model.getName())
					.orElseThrow(() -> notFoundByNameAndContainer(model, loadedLedger));
		}
		
		throw insufficientInfo(model);
	}

	private IllegalArgumentException insufficientInfo(Object modelObject) {
		return new IllegalArgumentException(
				String.format("Model Object does not provide sufficient information for loading original instance. %s",
						modelObject.toString()));
	}

	private IllegalArgumentException nullInfo() {
		return new IllegalArgumentException("Model object can not be null");
	}

	private NotFoundException notFoundById(BaseEntity model) {
		return new NotFoundException(
				String.format("Entity of type %s and id %s not found.", model.getClass().getName(), model.getId()));
	}

	private NotFoundException notFoundByNameAndContainer(NamedEntity model, NamedEntity container) {
		if (container == null)
			return new NotFoundException(
					String.format("Entity of type %s and name %s", model.getClass().getName(), model.getName()));
		return new NotFoundException(String.format("Entity of type %s and name %s from container %s not found.",
				model.getClass().getName(), model.getName(), container.getName()));
	}
}
