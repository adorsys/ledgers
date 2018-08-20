package de.adorsys.ledgers.postings.service.impl;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;

import de.adorsys.ledgers.postings.domain.ChartOfAccount;
import de.adorsys.ledgers.postings.domain.Ledger;
import de.adorsys.ledgers.postings.domain.LedgerAccountType;
import de.adorsys.ledgers.postings.repository.ChartOfAccountRepository;
import de.adorsys.ledgers.postings.repository.LedgerAccountRepository;
import de.adorsys.ledgers.postings.repository.LedgerAccountTypeRepository;
import de.adorsys.ledgers.postings.repository.LedgerRepository;
import de.adorsys.ledgers.postings.repository.PostingRepository;
import de.adorsys.ledgers.postings.utils.NamePatterns;

public class AbstractServiceImpl {

	@Autowired
	protected PostingRepository postingRepository;
	
	@Autowired
	protected LedgerAccountRepository ledgerAccountRepository;

	@Autowired
	protected ChartOfAccountRepository chartOfAccountRepo;

	@Autowired
	protected LedgerAccountTypeRepository ledgerAccountTypeRepo;

	@Autowired
	protected Principal principal;

	@Autowired
	protected NamePatterns namePatterns;

	@Autowired
	protected LedgerRepository ledgerRepository;

	protected Ledger loadLedger(Ledger model){
		if (model == null)
			throw new IllegalArgumentException(String.format("Missing field ledger in model object"));

		Ledger ledger = null;
		if (model.getId() != null)
			ledger = ledgerRepository.findById(model.getId()).orElseThrow(() -> new IllegalArgumentException(
					String.format("Can not find ledger with id %s", model.getId())));

		if (model.getName() != null)
			ledger = ledgerRepository.findOptionalByName(model.getName())
					.orElseThrow(() -> new IllegalArgumentException(
							String.format("Can not find ledger with name %s", model.getName())));
		
		if (ledger == null)
			throw new IllegalArgumentException(String.format(
					"Field ledger in model object must specify either the id or the name of an existing ledger."));
		
		return ledger;
	}
	
	protected ChartOfAccount loadCoa(ChartOfAccount model){
		if (model == null)
			throw new IllegalArgumentException(String.format("Missing field chart of account in model object"));

		ChartOfAccount coa = null;

		if (model.getId() != null)
			coa = chartOfAccountRepo.findById(model.getId()).orElseThrow(() -> new IllegalArgumentException(
					String.format("Can not find chart of account with id %s", model.getId())));

		if (model.getName() != null)
			coa = chartOfAccountRepo.findOptionalByName(model.getName())
					.orElseThrow(() -> new IllegalArgumentException(
							String.format("Can not find chart of account with name %s", model.getName())));
		
		if (coa == null)
			throw new IllegalArgumentException(String.format(
					"Field chart of account in model object must specify either the id or the name of an existing chart of account."));
		
		return coa;
	}
	
	protected LedgerAccountType loadAccountType(ChartOfAccount coa, LedgerAccountType accountType){
		// Load the ledger account type of ledger account
		if (accountType == null)
			throw new IllegalArgumentException(String.format("Missing account type in model."));
		
		LedgerAccountType ledgerAccountType = null;
		if(accountType.getId()!=null){
			ledgerAccountType = ledgerAccountTypeRepo.findById(accountType.getId())
					.orElseThrow(()->new IllegalArgumentException(
							String.format("Missing account type of ledger account with type with id %s", accountType.getId())));
			if(ledgerAccountType.getCoa().getId().equals(coa.getId())){
				throw new IllegalArgumentException(
						String.format("Account type with id %s hat another a the worng chart of account with id %s. Expected is %s", accountType.getId(), accountType.getCoa().getId(), coa.getId()));
			}
		} else if (accountType.getName()!=null){
			ledgerAccountType = ledgerAccountTypeRepo.findOptionalByCoaAndName(coa, accountType.getName())
					.orElseThrow(()->new IllegalArgumentException(
							String.format("Missing account type with coa %s and type name %s and valid at date %s", coa.getName(), accountType.getName())));
		}
		if (ledgerAccountType == null)
			throw new IllegalArgumentException(String.format(
					"Field account type in model object must specify either the id or the name of an existing account type."));
		
		return accountType;
		
	}
}
